package oo_11;

import java.awt.Point;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import javax.activity.InvalidActivityException;

/**
 * @ OVERVIEW: for convenient, put stay and filewriter into this class.
 * @ INVARIANT: None;
 */
class freq {
	@SuppressWarnings("static-access")
	/**
	 * Taxi Thread, used to control each taxi;
	 * @REQUIRES: None
	 * @MODIFIES: Thread.currentThread().sleep(time);
	 * @EFFECTS: \exist E e ==> None
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public static void stay(long time) {
		try {
			Thread.currentThread().sleep(time);
		} catch (InterruptedException e) {
			System.out.println("Sleep Error!");
		}
	}
	
	/**
	 * Taxi Thread, used to control each taxi;
	 * @REQUIRES: None;
	 * @MODIFIES: None;
	 * @EFFECTS:  \exist E e ==> None
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public synchronized static void filewrite(String string) {
		try {
//            FileWriter writer = new FileWriter(num+".txt", true);
			FileWriter writer = new FileWriter("detail.txt", true);
            writer.write(string);
            writer.write("\r\n");
            writer.close();
        } catch (IOException e) {
        	System.out.println("Write Error!");
//            e.printStackTrace();
        }
	}
	
	public boolean repOK() {
		return true;
	}
}

enum TaxiStatus{
	SERVING,PICKING,WAITING,STOPING;
}

/**
 * @ OVERVIEW: Taxi normal action with four status: SERVING, PICKING, WAITING, and STOPING.
 * @ INHERIT: <Thread> | <public void run()>
 * @ INVARIANT: None;
 */
public class Taxi extends Thread{
	int x;
	int y;
	Vector<String> availableway;
	int waitcount=0;
	int num;
	int[] flag = new int[666];
	public int credit=0;
	public Passenger passenger;
	TaxiGUI gui;
	TaxiStatus taxiStatus;
	Vector<Point> list0 = null;
	Vector<node> queue1;
	long time0;
	Point lastp = new Point(-1, -1);
	
	/**
	 * @REQUIRES: None
	 * @MODIFIES: None
	 * @EFFECTS: None;
	 */
	public Taxi() {}
	
	/**
	 * @REQUIRES: None
	 * @MODIFIES: num, x, y, gui, taxiStatus;
	 * @EFFECTS: \result == this && this.num == n && this.x == x0 && this.y == y0 && this.gui == gui0 && this.taxiStatus == taxiStatus.WAITING;
	 * 				gui.SetTaxiStatus(n, new Point(x, y), 2);
	 */
	public Taxi(int n, int x0, int y0, TaxiGUI gui0) {
		num = n;
		x = x0;
		y = y0;
		gui = gui0;
		taxiStatus = TaxiStatus.WAITING;
		gui.SetTaxiStatus(n, new Point(x, y), 2);
	}
	
	@SuppressWarnings("static-access")
	/**
	 * Taxi Thread, used to control each taxi;
	 * @REQUIRES: None
	 * @MODIFIES: this.taxiStatus;
	 * @EFFECTS:  
	 * 	(this.taxiStatus.equals(taxiStatus.SERVING)) ==> taxiserve() ==> this.taxiStatus = taxiStatus.STOPING;
	 * 	(this.taxiStatus.equals(taxiStatus.PICKING)) ==> taxipick() ==> this.taxiStatus = taxiStatus.SERVING;
	 * 	(this.taxiStatus.equals(taxiStatus.WAITING) && waitcount==40) ==> this.taxiStatus = taxiStatus.STOPING;
	 * 	(this.taxiStatus.equals(taxiStatus.STOPING)) ==> taxistop();updatewaitcount(0);this.taxiStatus = taxiStatus.WAITING;
	 * @THREAD_REQUIRES: None
	 * @THREAD_EFFECTS: None
	 */
	public void run() {
		try {
			while(true) {
				if(this.taxiStatus.equals(taxiStatus.SERVING)) {
					time0 = System.currentTimeMillis();
					list0 = gui.RequestTaxi(num, new Point(x, y), new Point(passenger.dstx, passenger.dsty),0);
					taxiserve();
					String string = "["+gv.getFormatTime()+"]:"+"dst_site:"+"("+x+","+y+")"+",arrive_time:"+System.currentTimeMillis();
					freq.filewrite(string);
					System.out.println(string);
					this.taxiStatus = taxiStatus.STOPING;
				}
				else if(this.taxiStatus.equals(taxiStatus.PICKING)) {
					time0 = System.currentTimeMillis();
					list0 = gui.RequestTaxi(num, new Point(x, y), new Point(passenger.srcx, passenger.srcy),0);
					freq.stay(1000);
					taxipick();
					String string = "["+gv.getFormatTime()+"]:"+"getpassenger_site:"+"("+x+","+y+")"+","+"getpassenger_time:"+System.currentTimeMillis();
					freq.filewrite(string);
					System.out.println(string);
					this.taxiStatus = taxiStatus.SERVING;
				}
				else if(this.taxiStatus.equals(taxiStatus.WAITING)) {
					time0 = System.currentTimeMillis();
					updatewaitcount(1);
					taxiwait();
					if(waitcount==40) {
						this.taxiStatus = taxiStatus.STOPING;
					}
					
				}
				else if(this.taxiStatus.equals(taxiStatus.STOPING)){
					taxistop();
					updatewaitcount(0);
					this.taxiStatus = taxiStatus.WAITING;
//					gui.SetTaxiStatus(this.num, new Point(x, y), 2);
				}
			}
		} catch(Exception e) {
			System.out.println("Taxi error!");
		}
		
	}
	
	/**
	 * Taxi serve;
	 * @REQUIRES: taxi.taxiStatus.equals("SERVING");
	 * @MODIFIES: this.x, this.y;
	 * @EFFECTS:  this.x == point.x; this.y == point.y;
	 */
	public void taxiserve() {
		for(int i=0;i<list0.size();i++) {
			Point point = list0.get(i);
			if(point.x==this.x && point.y==this.y) {
				continue;
			}
			if(guigv.m.graph[point.x*80+point.y][this.x*80+this.y]!=1) {
				list0 = gui.RequestTaxi(num, new Point(x, y), new Point(passenger.srcx, passenger.srcy),0);
				taxipick();
				return ;
			}
			lightjudge(point);
			freq.stay(500);
			this.lastp = new Point(x, y);
			gui.SetTaxiStatus(num, point, 1);
			updatesite(point.x, point.y);
			String fileinside = "["+gv.getFormatTime()+"]:No."+this.num+",serve_passby_site:"+"("+point.x+","+point.y+")";
			fileinside += ",serve_passby_time:"+System.currentTimeMillis()+passenger.getrequest();
			freq.filewrite(fileinside);
			System.out.println(fileinside);
		}
	}
	
	/**
	 * Taxi pick;
	 * @REQUIRES: taxi.taxiStatus.equals("PICKING");
	 * @MODIFIES: this.x, this.y;
	 * @EFFECTS:  this.x == point.x; this.y == point.y;
	 */
	public void taxipick() {
		gui.SetTaxiStatus(num, new Point(x, y), 0);
		for(int i=0;i<list0.size();i++) {
			Point point = list0.get(i);
			if(point.x==this.x && point.y==this.y) {
				continue;
			}
			if(guigv.m.graph[point.x*80+point.y][this.x*80+this.y]!=1) {
				list0 = gui.RequestTaxi(num, new Point(x, y), new Point(passenger.srcx, passenger.srcy),0);
				taxipick();
				return ;
			}
			lightjudge(point);
			freq.stay(500);
			this.lastp = new Point(x, y);
			gui.SetTaxiStatus(num, point, 3);
			if(i==list0.size()-1) {
				gui.SetTaxiStatus(num, point, 1);
			}
			updatesite(point.x, point.y);
//			freq.stay(500);			
			String fileinside = "["+gv.getFormatTime()+"]:No."+this.num+",pick_passby_site:"+"("+point.x+","+point.y+")";
			fileinside += ",pick_passby_time:"+System.currentTimeMillis()+passenger.getrequest();
			freq.filewrite(fileinside);
			System.out.println(fileinside);
		}
	}
	
	/**
	 * Taxi stop;
	 * @REQUIRES: None
	 * @MODIFIES: gui;
	 * @EFFECTS:  None
	 */
	public void taxistop() {		
		gui.SetTaxiStatus(this.num, new Point(x, y), 0);
		freq.stay(1000);
	}
	
	/**
	 * Taxi wait;
	 * @REQUIRES: None
	 * @MODIFIES: this.x,this.y;
	 * @EFFECTS:  this.x == x0, this.y == y0;
	 */
	public void taxiwait() {
		int up_flow=999;
		int down_flow=999;
		int left_flow=999;
		int right_flow=999;
		availableway = new Vector<String>();
		availableway.clear();
		if(x!=0) {
			if(x>79 || x<1 || y>79 || y<0) {
//				System.out.println(this.num+":x="+x+",y="+y);
			}
			if(TaxiMain.map[x-1][y]==2 || TaxiMain.map[x-1][y]==3) {
				up_flow = FlowFunc.GetFlow(x, y, x-1, y);
			}
		}
		if(x!=79) {
			if(x>79 || x<0 || y>79 || y<0) {
//				System.out.println("x="+x+",y="+y);
			}
			if(TaxiMain.map[x][y]==2 || TaxiMain.map[x][y]==3) {
				down_flow = FlowFunc.GetFlow(x, y, x+1, y);
			}
		}
		if(y!=0) {
			if(x>79 || x<0 || y>79 || y<1) {
//				System.out.println("x="+x+",y="+y);
			}
			if(TaxiMain.map[x][y-1]==1 || TaxiMain.map[x][y-1]==3) {
				left_flow = FlowFunc.GetFlow(x, y, x, y-1);
			}
		}
		if(y!=79) {
			if(x>79 || x<0 || y>79 || y<0) {
//				System.out.println("x="+x+",y="+y);
			}
			if(TaxiMain.map[x][y]==1 || TaxiMain.map[x][y]==3) {
				right_flow = FlowFunc.GetFlow(x, y, x, y+1);
			}
		}
		/*************************************************************Select By Flow***************************************/
		int min=999;
		if(up_flow<=min) {
			if(up_flow==min) {
				availableway.add("UP");
			} else {
				min = up_flow;
				availableway.clear();
				availableway.add("UP");
			}
		}
		if(down_flow<=min) {
			if(down_flow==min) {
				availableway.add("DOWN");
			} else {
				min = down_flow;
				availableway.clear();
				availableway.add("DOWN");
			}
		}
		if(left_flow<=min) {
			if(left_flow==min) {
				availableway.add("LEFT");
			} else {
				min = left_flow;
				availableway.clear();
				availableway.add("LEFT");
			}
		}
		if(right_flow<=min) {
			if(right_flow==min) {
				availableway.add("RIGHT");
			} else {
				min = right_flow;
				availableway.clear();
				availableway.add("RIGHT");
			}
		}
		
		Random random = new Random();
		int index = random.nextInt(availableway.size());
		String dir = availableway.get(index);
		if(dir.equals("UP")) {
			if(x<=0) {
				String content = "";
				for(int i=0;i<availableway.size();i++) {
					content += availableway.get(i) + " ";
				}
				System.out.println(content);
				return ;
			}
			lightjudge(new Point(x-1, y));
			freq.stay(500);
			this.lastp = new Point(x, y);
			gui.SetTaxiStatus(this.num, new Point(x-1, y), 2);
			updatesite(x-1, y);
			if(x<0) {
				gui.SetTaxiStatus(this.num, new Point(0, y), 2);
				updatesite(0, y);
			}
		}else if(dir.equals("DOWN")) {
			if(x>79) {
				String content = "";
				for(int i=0;i<availableway.size();i++) {
					content += availableway.get(i) + " ";
				}
				System.out.println(content);
				return ;
			}
			lightjudge(new Point(x+1, y));
			freq.stay(500);
			this.lastp = new Point(x, y);
			gui.SetTaxiStatus(this.num, new Point(x+1, y), 2);
			updatesite(x+1, y);
			if(x>79) {
				gui.SetTaxiStatus(this.num, new Point(79, y), 2);
				updatesite(79, y);
			}
		}else if(dir.equals("LEFT")) {
			if(y<=0) {
				String content = "";
				for(int i=0;i<availableway.size();i++) {
					content += availableway.get(i) + " ";
				}
				System.out.println(content);
				return ;
			}
			lightjudge(new Point(x, y-1));
			freq.stay(500);
			this.lastp = new Point(x, y);
			gui.SetTaxiStatus(this.num, new Point(x, y-1), 2);
			updatesite(x, y-1);
			if(y<0) {
				gui.SetTaxiStatus(this.num, new Point(x, 0), 2);
				updatesite(x, 0);
			}
		}else if(dir.equals("RIGHT")) {
			if(y>79) {
				String content = "";
				for(int i=0;i<availableway.size();i++) {
					content += availableway.get(i) + " ";
				}
				System.out.println(content);
				return ;
			}
			lightjudge(new Point(x, y+1));
			freq.stay(500);
			this.lastp = new Point(x, y);
			gui.SetTaxiStatus(this.num, new Point(x, y+1), 2);
			updatesite(x, y+1);
			if(y>79) {
				gui.SetTaxiStatus(this.num, new Point(x, 79), 2);
				updatesite(x, 79);
			}
		}
	}
	
	/**
	 * update site of taxi;
	 * @REQUIRES: None
	 * @MODIFIES: this.x = x0; this.y = y0;
	 * @EFFECTS:  None
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public synchronized void updatesite(int x0, int y0) {
		this.x = x0;
		this.y = y0;
	}
	
	/**
	 * get site information;
	 * @REQUIRES: None
	 * @MODIFIES: None
	 * @EFFECTS:  \result == "("+this.x+","+this.y+")";
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public synchronized String  sitetostring() {
		return "("+this.x+","+this.y+")";
	}
	
	/**
	 * get x information;
	 * @REQUIRES: None
	 * @MODIFIES: None
	 * @EFFECTS:  \result == this.x;
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public synchronized int getx() {
		return this.x;
	}
	
	/**
	 * get y information;
	 * @REQUIRES: None
	 * @MODIFIES: None
	 * @EFFECTS:  \result == this.y;
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public synchronized int gety() {
		return this.y;
	}
	
	/**
	 * @REQUIRES: None
	 * @MODIFIES: None
	 * @EFFECTS:  \result == new Point(x, y);
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public synchronized Point getpoint() {
		return new Point(x, y);
	}
	
	/**
	 * wait count update;
	 * @REQUIRES: None
	 * @MODIFIES: None;
	 * @EFFECTS: 
	 * (i==0) ==> waitcount = 0;
	 * (i==1) ==> waitcount++;
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public synchronized void updatewaitcount(int i) {
		switch (i) {
		case 0:
			waitcount = 0;
			break;
		case 1:
			waitcount++;
			break;
		default:
			break;
		}
	}
	
	/**
	 * @REQUIRES: None;
	 * @MODIFIES: None;
	 * @EFFECTS: 
	 * (lastp.y==nowp.y && (lastp.x-1)==nowp.x) ==> \result == "UP";
	 * (lastp.y==nowp.y && (lastp.x+1)==nowp.x) ==> \result == "DOWN";
	 * (lastp.x==nowp.x && (lastp.y-1)==nowp.y) ==> \result == "LEFT";
	 * (lastp.x==nowp.x && (lastp.y+1)==nowp.y ==> \result == "RIGHT";
	 * Rest Situation ==> \result == "ERROR";
	 */
	public String taxidir(Point lastp,Point nowp) {
		if(lastp.y==nowp.y) {
			if((lastp.x-1)==nowp.x) {
				return "UP";
			} else if((lastp.x+1)==nowp.x) {
				return "DOWN";
			}
		} else if(lastp.x==nowp.x) {
			if((lastp.y-1)==nowp.y) {
				return "LEFT";
			} else if((lastp.y+1)==nowp.y) {
				return "RIGHT";
			}
		}
		return "ERROR";
	}
	
	/**
	 * @REQUIRES: None;
	 * @MODIFIES: freq;
	 * @EFFECTS: freq.stay(LightControl.rest);
	 */
	public void lightjudge(Point newp) {
		if(LightControl.lightwait[this.x][this.y]!=null && LightControl.lightwait[this.x][this.y].exist) {
			if(taxidir(lastp, new Point(x, y)).equals(taxidir(new Point(x, y), newp))) {
				switch (taxidir(lastp, new Point(x, y))) {
				case "UP":
					if(LightControl.lightwait[this.x][this.y].color.equals(LightStatus.NS_RED)) {
						if(LightControl.rest>0) {
							freq.stay(LightControl.rest);
						}
					}
					break;
				case "DOWN":
					if(LightControl.lightwait[this.x][this.y].color.equals(LightStatus.NS_RED)) {
						if(LightControl.rest>0) {
							freq.stay(LightControl.rest);
						}
					}
					break;
				case "LEFT":
					if(LightControl.lightwait[this.x][this.y].color.equals(LightStatus.NS_GREEN)) {
						if(LightControl.rest>0) {
							freq.stay(LightControl.rest);
						}
					}
					break;
				case "RIGHT":
					if(LightControl.lightwait[this.x][this.y].color.equals(LightStatus.NS_GREEN)) {
						if(LightControl.rest>0) {
							freq.stay(LightControl.rest);
						}
					}
					break;
				default:
					System.out.println("Error!");
					break;
				}
			} else if(taxiturnleft(newp)) {
				switch (taxidir(lastp, new Point(x, y))) {
				case "UP":
					if(LightControl.lightwait[this.x][this.y].color.equals(LightStatus.NS_GREEN)) {
						if(LightControl.rest>0) {
							freq.stay(LightControl.rest);
						}
					}
					break;
				case "DOWN":
					if(LightControl.lightwait[this.x][this.y].color.equals(LightStatus.NS_GREEN)) {
						if(LightControl.rest>0) {
							freq.stay(LightControl.rest);
						}
					}
					break;
				case "LEFT":
					if(LightControl.lightwait[this.x][this.y].color.equals(LightStatus.NS_RED)) {
						if(LightControl.rest>0) {
							freq.stay(LightControl.rest);
						}
					}
					break;
				case "RIGHT":
					if(LightControl.lightwait[this.x][this.y].color.equals(LightStatus.NS_RED)) {
						if(LightControl.rest>0) {
							freq.stay(LightControl.rest);
						}
					}
					break;
				default:
					System.out.println("Error!");
					break;
				}
			}
		}
	}
	
	/**
	 * @REQUIRES: None;
	 * @MODIFIES: None;
	 * @EFFECTS: 
	 * (taxidir(lastp, new Point(x, y)).equals("RIGHT") && taxidir(new Point(x, y), newp).equals("UP")) ==> \result == true;
	 * (taxidir(lastp, new Point(x, y)).equals("UP") && taxidir(new Point(x, y), newp).equals("LEFT")) ==> \result == true;
	 * (taxidir(lastp, new Point(x, y)).equals("LEFT") && taxidir(new Point(x, y), newp).equals("DOWN")) ==> \result == true;
	 * (taxidir(lastp, new Point(x, y)).equals("DOWN") && taxidir(new Point(x, y), newp).equals("RIGHT")) ==> \result == true;
	 * Rest Situation ==> \result == false;
	 */
	public boolean taxiturnleft(Point newp) {
		if(taxidir(lastp, new Point(x, y)).equals("RIGHT") && taxidir(new Point(x, y), newp).equals("UP")) {
			return true;
		} else if(taxidir(lastp, new Point(x, y)).equals("UP") && taxidir(new Point(x, y), newp).equals("LEFT")) {
			return true;
		} else if(taxidir(lastp, new Point(x, y)).equals("LEFT") && taxidir(new Point(x, y), newp).equals("DOWN")) {
			return true;
		} else if(taxidir(lastp, new Point(x, y)).equals("DOWN") && taxidir(new Point(x, y), newp).equals("RIGHT")) {
			return true;
		}
		return false;
	}
	
	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		if(x<0 || x>79)	return false;
		if(y<0 && y>79)	return false;
		if(waitcount<0)	return false;
		if(num<0)	return false;
		if(credit<0)	return false;
		if(time0<0)	return false;
		return true;
	}
}
