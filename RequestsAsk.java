package oo_11;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.activity.InvalidActivityException;

/**
 * @ OVERVIEW: Open a time window for a request and wait for taxis to respond this request.
 * @ INHERIT: <Thread> | <public void run()>
 * @ INVARIANT: None;
 */
class RequestsAsk extends Thread{
	static int filecount=0;
	int num;
	Passenger passenger;
	CopyOnWriteArrayList<Taxi> okTaxis = new CopyOnWriteArrayList<Taxi>();
	
	/**
	 * @REQUIRES: None
	 * @MODIFIES: passenger;
	 * @EFFECTS: \result == this && this.passenger == p;
	 */
	public RequestsAsk(Passenger p) {
		passenger = p;
	}
	
	/**
	 * RequestAsk Thread, used to arrange passenger
	 * @REQUIRES: \all Taxi taxi; taxi.passenger != passenger
	 * @MODIFIES: taxi; 
	 * @EFFECTS: taxi.passenger == passenger; 
	 * @THREAD_REQUIRES: \this.credit
	 * @THREAD_EFFECTS: \this.credit
	 */
	public void run() {
		try {
			synchronized(this) {
				long start = System.currentTimeMillis();
				String fileinside = "["+gv.getFormatTime()+"]:"+"request_time:"+start/100+",request_src:"+"("+passenger.srcx+","+passenger.srcy+")";
				fileinside += ",request_dst:"+"("+passenger.dstx+","+passenger.dsty+")";
				filewrite(fileinside);
				System.out.println(fileinside);
				for(Taxi t:TaxiMain.taxis) {
					t.flag[num] = 0;
				}
				while((System.currentTimeMillis()-start)<7500) {
					for(Taxi t:TaxiMain.taxis) {
						if(canpick(t, passenger)) {
							okTaxis.add(t);
							t.passenger = passenger;
							synchronized(t) {
								t.credit++;
							}
							t.flag[num] = 1;
						}
					}
				}
				for(int i=0;i<okTaxis.size();i++) {
					Taxi t0 = okTaxis.get(i);
					if(!t0.taxiStatus.equals(TaxiStatus.WAITING)) {
						okTaxis.remove(i);
						i--;
						continue;
					}
				}
				if(okTaxis.size()==0) {
					fileinside = "["+gv.getFormatTime()+"]:"+"No Taxi Pick this Passenger"+passenger.getrequest()+"!";
					filewrite(fileinside);
					System.out.println(fileinside);
					return ;
				} else {
					for(int i=0;i<okTaxis.size();i++) {
						Taxi t0 = okTaxis.get(i);
						if(!t0.taxiStatus.equals(TaxiStatus.WAITING)) {
							okTaxis.remove(i);
							i--;
							continue;
						}
						fileinside = "["+gv.getFormatTime()+"]:"+"car_id: "+t0.num+" Can Pick this Request"+passenger.getrequest()+",car_site: "+t0.sitetostring();
						fileinside += ",car_status: WAITING"+",car_credit: "+t0.credit;
						filewrite(fileinside);
						System.out.println(fileinside);
						if(t0.num<TaxiMain.SPECIALNUM) {
							SpecialTaxi t1 = (SpecialTaxi) t0;
							String string = "["+gv.getFormatTime()+"]:car_id: "+t1.num+" try to get "+passenger.getrequest()+" with Info -> "+passenger.getreqInfo();
							t1.trace.add(string);
						}
					}
					if(okTaxis.size()==0) {
						fileinside = "["+gv.getFormatTime()+"]:"+"No Taxi Pick this Passenger"+passenger.getrequest()+"!";
						filewrite(fileinside);
						System.out.println(fileinside);
						return ;
					}
					Taxi tget = rank(okTaxis);
					fileinside = "["+gv.getFormatTime()+"]:"+"car_id: "+tget.num+" Get this Request"+passenger.getrequest();
					fileinside += ",car_site: "+tget.sitetostring();
					filewrite(fileinside);
					System.out.println(fileinside);
					if(tget.num<TaxiMain.SPECIALNUM) {
						SpecialTaxi t1 = (SpecialTaxi) tget;
						t1.trace.add(fileinside);
					}
					tget.taxiStatus = TaxiStatus.PICKING;
					synchronized(this) {
						tget.credit += 3;
					}
					tget.passenger = passenger;
				}
			}
		} catch(Exception e) {
			System.out.println("RequestsAsk error!");
		}
		
	}
	
	/**
	 * write string to correspond file
	 * @REQUIRES: \exist new File("detail.txt");
	 * @MODIFIES: writer;
	 * @EFFECTS: writer.write(string+"\r\n");writer.close();
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public synchronized void filewrite(String string) {
		try {
	        FileWriter writer = new FileWriter("detail.txt", true);
	        writer.write(string);
	        writer.write("\r\n");
	        writer.close();
	    } catch (IOException e) {
	    	System.out.println("Write Error!");
	        e.printStackTrace();
	    }
	}
	
	/**
	 * judge whether this taxi can pick this passenger request;
	 * @REQUIRES: \exist Taxi t; \exist Passenger p
	 * @MODIFIES: writer.write(string+"\r\n");writer.close();
	 * @EFFECTS: None 
	 */
	public boolean canpick(Taxi t,Passenger p) {
		if((p.srcx-t.getx())>=-2 && (p.srcx-t.getx())<=2 && (p.srcy-t.gety())>=-2 && (p.srcy-t.gety())<=2 && t.taxiStatus.equals(TaxiStatus.WAITING) && t.flag[num]==0) {
			return true;
		}
		return false;
	}
	
	/**
	 * rank taxis by credit and distance;
	 * @REQUIRES: \exist CopyOnWriteArrayList<Taxi> t;
	 * @MODIFIES: Collections.sort(t, new SortByRule());
	 * @EFFECTS: \result == t.get(0); 
	 */
	public Taxi rank(CopyOnWriteArrayList<Taxi> t) {
		Collections.sort(t,new SortByRule());
		return t.get(0);
	}
	
	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		if(RequestsAsk.filecount<0)	return false;
		if(num<0)	return false;
		if(TaxiMain.taxis==null)	return false;
		return true;
	}
}

/**
 * @ OVERVIEW: Sort Taxi as credit and distance.
 * @ INVARIANT: None;
 */
class SortByRule implements Comparator<Taxi> {
	/**
	 * compare two taxi by credits and distance
	 * @REQUIRES: t1 != null; t2 != null;
	 * @MODIFIES: None
	 * @EFFECTS:  
	 * (taxi1.credit > taxi2.credit) ==> \result = -1;
	 * (taxi1.credit==taxi2.credit && d1<d2) ==> \result = -1;
	 * (taxi1.credit==taxi2.credit && d1==d2) ==> \result = 0;
	 * (taxi1.credit==taxi2.credit && d1>d2) ==> \result = 1;
	 * (taxi1.credit <= taxi2.credit) ==> \result = 1;
	 */
	public int compare(Taxi t1,Taxi t2) {
		Taxi taxi1 = (Taxi)t1;
		Taxi taxi2 = (Taxi)t2;
		if(taxi1.credit>taxi2.credit) {
			return -1;
		}else if(taxi1.credit==taxi2.credit) {
			double d1 = getd(taxi1.getx(), taxi1.gety(), taxi1.passenger.srcx, taxi1.passenger.srcy);
			double d2 = getd(taxi2.getx(), taxi2.gety(), taxi2.passenger.srcx, taxi2.passenger.srcy);
			if(d1<d2) {
				return -1;
			}else if(d1==d2) {
				return 0;
			}else {
				return 1;
			}
		}else {
			return 1;
		}

	}
	
	/**
	 * count the distance between (x1,y1) and (x2,y2);
	 * @REQUIRES: None
	 * @MODIFIES: None
	 * @EFFECTS:  \result == Math.sqrt(x*x+y*y);
	 */
	public double getd(int x1,int y1,int x2,int y2) {
		double x = Math.abs(x1-x2);
		double y = Math.abs(y1-y2);
		return Math.sqrt(x*x+y*y);
	}
	
	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		return true;
	}
	
}
