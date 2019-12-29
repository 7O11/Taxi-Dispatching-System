package oo_11;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activity.InvalidActivityException;

/**
 * @ OVERVIEW: Get instructions from console, and do corresponding action.
 * @ INHERIT: <Thread> | <public void run()>
 * @ INVARIANT: None;
 */
public class RequestsGet extends Thread{
	TaxiGUI gui;
	TaxiMain taxiMain;
	Passenger last=null;
	private int filerequestflag=0;
	LightControl lightControl = new LightControl();
	DealSameRequest dSameRequest = new DealSameRequest();
	
	/**
	 * @REQUIRES: None
	 * @MODIFIES: gui,taxiMain;
	 * @EFFECTS: \result == this && this.gui == g && this.taxiMain == tm;
	 */
	public RequestsGet(TaxiGUI g, TaxiMain tm) {
		gui = g;
		taxiMain = tm;
	}
	
	/**
	 * RequestGet Thread, used to read from controller table;
	 * @REQUIRES: None
	 * @MODIFIES: p,requestsAsk;  
	 * @EFFECTS:  requestsAsk.start();
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public void run() {
		try {
			Scanner in = new Scanner(System.in);	
			while(true) {
				String string = in.nextLine();
				if(string.equals("DONE"))	break;
				String reg = "\\[CR\\,\\([1-7]?[0-9]\\,[1-7]?[0-9]\\)\\,\\([1-7]?[0-9]\\,[1-7]?[0-9]\\)\\]";
				Pattern pattern = Pattern.compile(reg);
				Matcher matcher = pattern.matcher(string);
				if(!matcher.matches()) {
					if(isfilerequest(string)) {
						if(filerequestflag==1) {
							System.out.println(string);
							System.out.println("["+gv.getFormatTime()+"]:"+"load instriction only used at the beginning!");
						}
						filerequestflag = 1;
						continue;
					}
					if(isrouterequest(string)) {
						filerequestflag = 1;
						continue;
					}
					if(isaskSpecialTaxi(string)) {
						filerequestflag = 1;
						continue;
					}
					if(isaskTaxiInfo(string)) {
						filerequestflag = 1;
						continue;
					}
					if(isaskStatus(string)) {
						filerequestflag = 1;
						continue;
					}
					System.out.println("["+gv.getFormatTime()+"] "+string+"-Error: Request Format Error!");
					continue;
				}
				dealrequest(string,1);
			}
			in.close();
		} catch(Exception e) {
			System.out.println("RequestsGet error!");
		}
	}
	
	/**
	 * @REQUIRES: None;
	 * @MODIFIES: None;
	 * @EFFECTS:  
	 * 	(divide[0].equals("GetSpecialTaxiServeInfo") && Integer.parseInt(divide[1])>=0 && Integer.parseInt(divide[1])<=29) ==> \result == true;
	 * 	(!divide[0].equals("GetSpecialTaxiServeInfo") || !(Integer.parseInt(divide[1])>=0 && Integer.parseInt(divide[1])<=29)) ==> \result == false;
	 */
	public boolean isaskSpecialTaxi(String str) {
		try {
			String[] divide = str.split(" ");
			if(divide[0].equals("GetSpecialTaxiServeInfo")) {
				if(Integer.parseInt(divide[1])>=0 && Integer.parseInt(divide[1])<=29) {
					int n = Integer.parseInt(divide[1]);
					SpecialTaxi specialTaxi = (SpecialTaxi) TaxiMain.taxis.get(n);
					if(specialTaxi.trace.size()==0) {
						System.out.println("Taxi No."+specialTaxi.num+" did not have any serve Info!");
						return true;
					}
					for(String str0:specialTaxi.trace) {
						System.out.println(str0);
					}
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	/**
	 * @REQUIRES: None;
	 * @MODIFIES: None;
	 * @EFFECTS:  
	 * 	(divide[0].equals("GetTaxiInfo") && Integer.parseInt(divide[1])>=0 && Integer.parseInt(divide[1])<=99) ==> \result == true;
	 * 	(!divide[0].equals("GetTaxiInfo") || !(Integer.parseInt(divide[1])>=0 && Integer.parseInt(divide[1])<=99)) ==> \result == false;
	 */
	public boolean isaskTaxiInfo(String str) {
		try {
			String[] divide = str.split(" ");
			if(divide[0].equals("GetTaxiInfo")) {
				if(Integer.parseInt(divide[1])>=0 && Integer.parseInt(divide[1])<=99) {
					int n = Integer.parseInt(divide[1]);
					Taxi taxi = TaxiMain.taxis.get(n);
					System.out.println("["+gv.getFormatTime()+"]: Taxi No."+taxi.num+" now at -> "+taxi.sitetostring()+"; Status -> "+taxi.taxiStatus);
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	/**
	 * @REQUIRES: None;
	 * @MODIFIES: None;
	 * @EFFECTS:  
	 * 	(divide[0].equals("GetStatusTaxis") && (divide[1].equals(TaxiStatus.PICKING.toString()) || divide[1].equals(TaxiStatus.SERVING.toString()) 
	 * 		|| divide[1].equals(TaxiStatus.STOPING.toString()) || divide[1].equals(TaxiStatus.WAITING.toString()))) ==> \result == true;
	 * 	(!divide[0].equals("GetStatusTaxis") || !(divide[1].equals(TaxiStatus.PICKING.toString()) || divide[1].equals(TaxiStatus.SERVING.toString()) 
	 * 		|| divide[1].equals(TaxiStatus.STOPING.toString()) || divide[1].equals(TaxiStatus.WAITING.toString()))) ==> \result == false;
	 */
	public boolean isaskStatus(String str) {
		int flag = 0;
		try {
			String[] divide = str.split(" ");
			if(divide[0].equals("GetStatusTaxis")) {
				if(divide[1].equals(TaxiStatus.PICKING.toString()) || divide[1].equals(TaxiStatus.SERVING.toString()) || divide[1].equals(TaxiStatus.STOPING.toString()) || divide[1].equals(TaxiStatus.WAITING.toString())) {
					for(Taxi t:TaxiMain.taxis) {
						if(t.taxiStatus.toString().equals(divide[1])) {
							flag = 1;
							System.out.println("Taxi No."+t.num+"; Status: "+t.taxiStatus+".");
						}
					}
					if(flag==0) {
						System.out.println("No Taxi at "+divide[1]+".");
					}
					return true;
				}
			}
		} catch(Exception e) {
			return false;
		}
		return false;
	}
	
	/**
	 * judge type of this request
	 * @REQUIRES: None
	 * @MODIFIES: gui,taxiMain;
	 * @EFFECTS: 
	 * (x1>=x2) ==> gui.SetRoadStatus(new Point(x1, y1), new Point(x2, y2), status);
	 * (x1>=x2) ==> taxiMain.setRoadStatus(new Point(x1, y1), new Point(x2, y2), status);
	 * (x1<x2) ==> gui.SetRoadStatus(new Point(x2, y2), new Point(x1, y1), status);
	 * (x1<x2) ==> taxiMain.setRoadStatus(new Point(x2, y2), new Point(x1, y1), status);
	 * \result == (matcher.matches());
	 */
	public boolean isrouterequest(String str) {
		String reg = "\\([1-7]?[0-9]\\,[1-7]?[0-9]\\)\\,\\([1-7]?[0-9]\\,[1-7]?[0-9]\\)\\,[01]";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(str);
		if(!matcher.matches()) {
			return false;
		}
		String[] divide = str.split("[(,)]");
		int x1 = Integer.parseInt(divide[1]);
		int y1 = Integer.parseInt(divide[2]);
		int x2 = Integer.parseInt(divide[5]);
		int y2 = Integer.parseInt(divide[6]);
		int status = Integer.parseInt(divide[8]);
		if((Math.abs(x1-x2)==1 && y1==y2) || (Math.abs(y1-y2)==1 && x1==x2)) {
			if(x1>=x2) {
				gui.SetRoadStatus(new Point(x1, y1), new Point(x2, y2), status);
				taxiMain.setRoadStatus(new Point(x1, y1), new Point(x2, y2), status);
			} else {
				gui.SetRoadStatus(new Point(x2, y2), new Point(x1, y1), status);
				taxiMain.setRoadStatus(new Point(x2, y2), new Point(x1, y1), status);
			}
			FlowFunc.ClearFlow(x1, y1, x2, y2);
		}else {
			System.out.println("["+gv.getFormatTime()+"]Error: This two point not share a road!");
		}
		return true;
	}
	
	/**
	 * judge type of this request
	 * @REQUIRES: None
	 * @MODIFIES: None
	 * @EFFECTS: \result == (str.length>5 && formatcheck.equals("Load ") && filerequest.exists());
	 */
	public boolean isfilerequest(String str) {
		String filename = null;
		String formatcheck = null;
		try {
			filename = str.substring(5);
			formatcheck = str.substring(0, 5);
		}catch(Exception e){
			return false;
		}
		if(!formatcheck.equals("Load ")) {
			return false;
		}
		File filerequest = new File(filename);
		if(filerequest.exists()) {
			if(filerequestflag==1) {
				return true;
			}
			if(!fileaction(filename)) {
				return false;				
			}
			return true;
		}
		return false;
	}
	
	/**
	 * file request get;
	 * @REQUIRES: None
	 * @MODIFIES: newfilename;
	 * @EFFECTS: 
	 * (!newfilename.equals("map.txt") && taxiMain.isFileMatch(map,newfilename)) ==> gui.MapUpdate(map, 80);
	 * \exist String eachline; getflowinfo(eachline);
	 * \exist String eachline; gettaxiinfo(eachline);
	 * \exist String eachline; dealrequest(eachline);
	 */
	public boolean fileaction(String str) {
		String eachline;
		FileInputStream fi = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fi = new FileInputStream(str);
			isr = new InputStreamReader(fi);
			br = new BufferedReader(isr);	
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			System.out.println("file not found");
			return false;
		}
		try {
////////////////////////////////////////////////////////test////////////////////////////////////////////
//System.out.println("getfilerequest0");
			/*******************************************map_in_file***********************************/
			while(!(eachline=br.readLine()).equals("#map")) {}
			eachline = br.readLine();
			if(!eachline.equals("#end_map")) {
				String newfilename = eachline;
				if(!taxiMain.isFileMatch(newfilename)) {
					System.out.println("["+gv.getFormatTime()+"]:"+"map file Error!");
					br.close();
					isr.close();
					fi.close();
					return false;
				}
				gui.MapUpdate(TaxiMain.map, 80);
//				taxiMain.updateMap(map,80);
			}
////////////////////////////////////////////////////////test////////////////////////////////////////////
//System.out.println("getfilerequest1");
			/*******************************************light_in_file***********************************/
			while(!(eachline=br.readLine()).equals("#light")){}
			eachline = br.readLine();
			if(!eachline.equals("#end_light")) {
				if(!lightfileinfo(eachline)) {
					System.out.println("["+gv.getFormatTime()+"]:"+eachline+" Error!");
					br.close();
					isr.close();
					fi.close();
					return false;
				}
			}
////////////////////////////////////////////////////////test////////////////////////////////////////////
//System.out.println("getfilerequest2");
			/*******************************************flow_in_file***********************************/
			while(!(eachline=br.readLine()).equals("#flow")) {}
			eachline = br.readLine();
			while(!eachline.equals("#end_flow")) {
				//updateflow
				getflowinfo(eachline);
				eachline = br.readLine();
			}
////////////////////////////////////////////////////////test////////////////////////////////////////////
//System.out.println("getfilerequest3");
			/*******************************************taxi_in_file***********************************/
			while(!(eachline=br.readLine()).equals("#taxi")) {}
			eachline = br.readLine();
			while(!eachline.equals("#end_taxi")) {
				gettaxiinfo(eachline);
				eachline = br.readLine();
			}
////////////////////////////////////////////////////////test////////////////////////////////////////////
//System.out.println("getfilerequest4");
			/*******************************************request_in_file***********************************/
			while(!(eachline=br.readLine()).equals("#request")) {}
			eachline = br.readLine();
			while(!eachline.equals("#end_request")) {
				//updaterequest
				 dealrequest(eachline,0);
				 eachline = br.readLine();
			}
			
		} catch (IOException e) {
			System.out.println("["+gv.getFormatTime()+"]"+"Error: Please use correct file and file format!");
			freq.filewrite("["+gv.getFormatTime()+"]"+"Error: Please use correct file and file format!");
		}catch(Exception e) {
			System.out.println("["+gv.getFormatTime()+"]"+"Error: Please use correct file and file format!");
			freq.filewrite("["+gv.getFormatTime()+"]"+"Error: Please use correct file and file format!");
		}
		
		/***************************************File Close*****************************************/
		try {
			br.close();
			isr.close();
			fi.close();
		}catch(IOException e) {
			System.out.println("File close failed!");
		}
		return true;
	}
	
	/**
	 * request deal;
	 * @REQUIRES: None
	 * @MODIFIES: requestAsk;
	 * @EFFECTS: (requestsAsk = new RequestsAsk(TaxiMain.taxis, p)) ==> requestsAsk.start();
	 */
	public void dealrequest(String str,int type) {	//(type==0)=>requests in file; (type==1)=>requests in controller
		String[] info = str.split("(\\[|\\,|\\]|\\(|\\))");
		Passenger p = new Passenger(Integer.parseInt(info[3]), Integer.parseInt(info[4]), Integer.parseInt(info[7]), Integer.parseInt(info[8]), System.currentTimeMillis()/100);
		DealSameRequest.reqlist.add(p);
		if(p.srcx==p.dstx && p.srcy==p.dsty) {
			System.out.println("["+gv.getFormatTime()+"]"+str+"-Error:Start point equals to End point!");
			return ;
		}
		if(dSameRequest.isfindsame()) {
			System.out.println("["+gv.getFormatTime()+"]"+str+"-Error: Same Request!");
			return ;
		}
		last = p;
		if(type==1) {
			filerequestflag = 1;
		}
		RequestsAsk requestsAsk = new RequestsAsk(p);
		requestsAsk.start();
	}
	
	/**
	 * @REQUIRES: None
	 * @MODIFIES: LightControl;
	 * @EFFECTS:
	 * File not correct ==> \result == false;
	 * update light info successful ==> \result == true; 
	 */
	public boolean lightfileinfo(String str) {
		int[][] l = new int[85][85];
		FileInputStream fi = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		Random random = new Random();
		/***************************************Check File Exist*****************************************/
		try {
			fi = new FileInputStream(str);
			isr = new InputStreamReader(fi);
			br = new BufferedReader(isr);	
		} catch (FileNotFoundException e) {
			return false;
		}
		/***************************************Read File*****************************************/
		try {
			for(int i=0;i<80;i++) {
				String line = br.readLine();
				for(int j=0;j<line.length();j++) {		
					char c = line.charAt(j);
					if(c!='0' && c!='1' && c!='2') {
						br.close();
						isr.close();
						fi.close();
						return false;
					}
					l[i][j] = Integer.parseInt(String.valueOf(c));
				}
			}
		} catch (IOException e) {
			return false;
		}
		
		/***************************************File Close*****************************************/
		try {
			br.close();
			isr.close();
			fi.close();
		}catch(IOException e) {
			return false;
		}
		int timelong = random.nextInt(501)+500;
		LightControl.lasttime = timelong;
		LightControl.starttime = System.currentTimeMillis();
		for(int i=0;i<80;i++) {
			for(int j=0;j<80;j++) {
				LightControl.lightwait[i][j] = new Light(i, j);
				if(l[i][j]==1) {
					if(iscrossroad(new Point(i, j))) {
						LightStatus c = null;
						int judgenum = random.nextInt(2);
						if(judgenum==0) {
							c = LightStatus.NS_RED;
							gui.SetLightStatus(new Point(i, j), 1);
						}else if(judgenum==1){
							c = LightStatus.NS_GREEN;
							gui.SetLightStatus(new Point(i, j), 2);
						}
						LightControl.lightwait[i][j].exist = true;
						LightControl.lightwait[i][j].color = c;
					} else {
						System.out.println("["+gv.getFormatTime()+"]: Light cannot put ("+i+","+j+")");
						freq.filewrite("["+gv.getFormatTime()+"]: Light cannot put ("+i+","+j+")");
					}
					
				}
			}
		}
		lightControl.start();
		return true;
	}
	
	/**
	 * new flow information get;
	 * @REQUIRES: None
	 * @MODIFIES: guigv;
	 * @EFFECTS: 
	 * guigv.AddFlow(px1, py1, px2, py2, flow);
	 * (\exist E e ==> None);
	 */
	public void getflowinfo(String str) {
		String[] info0 = str.split(" ");
		try {
			String[] point1 = info0[0].split("[(,)]");
			String[] point2 = info0[1].split("[(,)]");
			int px1 = Integer.parseInt(point1[1]);
			int py1 = Integer.parseInt(point1[2]);
			int px2 = Integer.parseInt(point2[1]);
			int py2 = Integer.parseInt(point2[2]);
			int flow = Integer.parseInt(info0[2]);
//			guigv.AddFlow(px1, py1, px2, py2, flow);
			FlowFunc.AddFlow(px1, py1, px2, py2, flow);
		} catch(Exception e) {
			System.out.println("Please insure you give me file which have correct format!");
		}
	}
	
	/**
	 * new taxi information get;
	 * @REQUIRES: None
	 * @MODIFIES: taxi.passenger,taxi.taxiStatus;
	 * @EFFECTS: 
	 * (status == 0) ==> taxi.passenger = p0; taxi.taxiStatus = TaxiStatus.SERVING;
	 * (status == 1) ==> taxi.passenger = p0; taxi.taxiStatus = TaxiStatus.PICKING;
	 * (status == 2) ==> taxi.taxiStatus = TaxiStatus.WAITING;
	 * (status == 3) ==> taxi.taxiStatus = TaxiStatus.STOPING;
	 * taxi.credit == creditinfo;
	 * taxi.updatesite(xinfo, yinfo);
	 */
	public void gettaxiinfo(String str) {
		Passenger p0 = new Passenger(0,0,1,0,System.currentTimeMillis()/100);
		String[] info0 = str.split(" ");
		String[] getn = info0[0].split("[.]");
		int n = Integer.parseInt(getn[1]);
		int status = Integer.parseInt(info0[1]);
		int creditinfo = Integer.parseInt(info0[2]);
		String[] point = info0[3].split("[(,)]");
		int xinfo = Integer.parseInt(point[1]);
		int yinfo = Integer.parseInt(point[2]);
		Taxi taxi = TaxiMain.taxis.get(n);
		switch (status) {
		case 0:
			taxi.passenger = p0;
			taxi.taxiStatus = TaxiStatus.SERVING;
			gui.SetTaxiStatus(taxi.num, new Point(xinfo, yinfo), 1);
			taxi.updatesite(xinfo, yinfo);
			break;
		case 1:
			taxi.passenger = p0;
			taxi.taxiStatus = TaxiStatus.PICKING;
			gui.SetTaxiStatus(taxi.num, new Point(xinfo, yinfo), 3);
			taxi.updatesite(xinfo, yinfo);
			break;
		case 2:
			taxi.taxiStatus = TaxiStatus.WAITING;
			gui.SetTaxiStatus(taxi.num, new Point(xinfo, yinfo), 2);
			taxi.updatesite(xinfo, yinfo);
			break;
		case 3:
			taxi.taxiStatus = TaxiStatus.STOPING;
			gui.SetTaxiStatus(taxi.num, new Point(xinfo, yinfo), 0);
			taxi.updatesite(xinfo, yinfo);
			break;
		default:
			break;
		}
		taxi.credit = creditinfo;
	}
	
	/**
	 * @REQUIRES: None
	 * @MODIFIES: None;
	 * @EFFECTS: 
	 * (count>=3) ==> \result == true;
	 * (count<3) ==> \result == false;
	 */
	public boolean iscrossroad(Point p) {
		int count=0;
		if(p.x>0 && guigv.m.graph[p.x*80+p.y][(p.x-1)*80+p.y]==1) {
			count++;
		}
		if(p.x<79 && guigv.m.graph[p.x*80+p.y][(p.x+1)*80+p.y]==1) {
			count++;
		}
		if(p.y<79 && guigv.m.graph[p.x*80+p.y][p.x*80+p.y+1]==1) {
			count++;
		}
		if(p.y>0 && guigv.m.graph[p.x*80+p.y][p.x*80+p.y-1]==1) {
			count++;
		}
		if(count>=3) {
			return true;
		}
		return false;
	}
	
	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		if(TaxiMain.taxis==null)	return false;
		if(gui==null)	return false;
		if(taxiMain==null)	return false;
		if(last==null)	return false;
		if(filerequestflag!=0 && filerequestflag!=1) {
			return false;
		}
		if(lightControl==null)	return false;
		if(dSameRequest==null)	return false;
		return true;
	}
}
