package oo_11;

import java.awt.Point;
import java.util.Random;
import java.util.Vector;

import javax.activity.InvalidActivityException;

/**
 * @ OVERVIEW: Special taxi action that it can go through closed road and the serve of it will be record.
 * @ INHERIT: <Taxi> | <public void run()> <public void taxiserve()> <public void taxipick()> <public void taxiwait()> <public boolean repOK()>
 * @ INVARIANT: None;
 */
public class SpecialTaxi extends Taxi{
	Vector<String> trace = new Vector<>();
	public SpecialTaxi(int n, int x0, int y0, TaxiGUI gui0) {
		super(n, x0, y0, gui0);
	}
	
	/**
	 * Special Taxi Thread, used to control each special taxi;
	 * @REQUIRES: None
	 * @MODIFIES: this.taxiStatus,trace;
	 * @EFFECTS:  
	 * 	(this.taxiStatus.equals(taxiStatus.SERVING)) ==> taxiserve() ==> trace.add(string);this.taxiStatus = taxiStatus.STOPING;
	 * 	(this.taxiStatus.equals(taxiStatus.PICKING)) ==> taxipick() ==> trace.add(string);this.taxiStatus = taxiStatus.SERVING;
	 * 	(this.taxiStatus.equals(taxiStatus.WAITING) && waitcount==40) ==> this.taxiStatus = taxiStatus.STOPING;
	 * 	(this.taxiStatus.equals(taxiStatus.STOPING)) ==> taxistop();updatewaitcount(0);this.taxiStatus = taxiStatus.WAITING;
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public void run() {
		try {
			while(true) {
				if(this.taxiStatus.equals(TaxiStatus.SERVING)) {
					time0 = System.currentTimeMillis();
					list0 = gui.RequestTaxi(num, this.getpoint(), new Point(passenger.dstx, passenger.dsty),1);
					taxiserve();
					String string = "["+gv.getFormatTime()+"]:"+"dst_site:"+this.sitetostring()+",arrive_time:"+System.currentTimeMillis();
					freq.filewrite(string);
					trace.add(string);
					System.out.println(string);
					this.taxiStatus = TaxiStatus.STOPING;
				}
				else if(this.taxiStatus.equals(TaxiStatus.PICKING)) {
					time0 = System.currentTimeMillis();
					list0 = gui.RequestTaxi(num, this.getpoint(), new Point(passenger.srcx, passenger.srcy),1);
					freq.stay(1000);
					taxipick();
					String string = "["+gv.getFormatTime()+"]:"+"getpassenger_site:"+this.sitetostring()+","+"getpassenger_time:"+System.currentTimeMillis();
					freq.filewrite(string);
					trace.add(string);
					System.out.println(string);
					this.taxiStatus = TaxiStatus.SERVING;
				}
				else if(this.taxiStatus.equals(TaxiStatus.WAITING)) {
					time0 = System.currentTimeMillis();
					updatewaitcount(1);
					taxiwait();
					if(waitcount==40) {
						this.taxiStatus = TaxiStatus.STOPING;
					}
					
				}
				else if(this.taxiStatus.equals(TaxiStatus.STOPING)){
					taxistop();
					updatewaitcount(0);
					this.taxiStatus = TaxiStatus.WAITING;
				}
			}
		} catch(Exception e) {
			System.out.println("Taxi error!");
		}
	}
	
	/**
	 * Special Taxi serve;
	 * @REQUIRES: taxi.taxiStatus.equals("SERVING");
	 * @MODIFIES: this.x, this.y, trace;
	 * @EFFECTS:  
	 * 	this.x == point.x; this.y == point.y;
	 * 	trace.add(fileinside);
	 */
	public void taxiserve() {
		for(int i=0;i<list0.size();i++) {
			Point point = list0.get(i);
			if(point.x==this.getx() && point.y==this.gety()) {
				continue;
			}
			lightjudge(point);
			freq.stay(500);
			this.lastp = this.getpoint();
			gui.SetTaxiStatus(num, point, 1);
			updatesite(point.x, point.y);
			String fileinside = "["+gv.getFormatTime()+"]:No."+this.num+",serve_passby_site:"+"("+point.x+","+point.y+")";
			fileinside += ",serve_passby_time:"+System.currentTimeMillis()+passenger.getrequest();
			freq.filewrite(fileinside);
			trace.add(fileinside);
			System.out.println(fileinside);
		}
	}
	
	/**
	 * Special Taxi pick;
	 * @REQUIRES: taxi.taxiStatus.equals("PICKING");
	 * @MODIFIES: this.x, this.y;
	 * @EFFECTS:  
	 * 	this.x == point.x; this.y == point.y;
	 * 	trace.add(fileinside);
	 */
	public void taxipick() {
		gui.SetTaxiStatus(num, this.getpoint(), 0);
		for(int i=0;i<list0.size();i++) {
			Point point = list0.get(i);
			if(point.x==this.getx() && point.y==this.gety()) {
				continue;
			}
			lightjudge(point);
			freq.stay(500);
			this.lastp = this.getpoint(); 
			gui.SetTaxiStatus(num, point, 3);
			if(i==list0.size()-1) {
				gui.SetTaxiStatus(num, point, 1);
			}
			updatesite(point.x, point.y);	
			String fileinside = "["+gv.getFormatTime()+"]:No."+this.num+",pick_passby_site:"+"("+point.x+","+point.y+")";
			fileinside += ",pick_passby_time:"+System.currentTimeMillis()+passenger.getrequest();
			freq.filewrite(fileinside);
			trace.add(fileinside);
			System.out.println(fileinside);
		}
	}
	
	/**
	 * Special Taxi wait;
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
			if(x>79 || x<1 || y>79 || y<0) {}
			if(TaxiMain.map0[x-1][y]==2 || TaxiMain.map0[x-1][y]==3) {
				up_flow = FlowFunc.GetFlow(x, y, x-1, y);
			}
		}
		if(x!=79) {
			if(x>79 || x<0 || y>79 || y<0) {}
			if(TaxiMain.map0[x][y]==2 || TaxiMain.map0[x][y]==3) {
				down_flow = FlowFunc.GetFlow(x, y, x+1, y);
			}
		}
		if(y!=0) {
			if(x>79 || x<0 || y>79 || y<1) {
//				System.out.println("x="+x+",y="+y);
			}
			if(TaxiMain.map0[x][y-1]==1 || TaxiMain.map0[x][y-1]==3) {
				left_flow = FlowFunc.GetFlow(x, y, x, y-1);
			}
		}
		if(y!=79) {
			if(x>79 || x<0 || y>79 || y<0) {}
			if(TaxiMain.map0[x][y]==1 || TaxiMain.map0[x][y]==3) {
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
