package oo_11;

import java.util.Vector;

import javax.activity.InvalidActivityException;

/**
 * @ OVERVIEW: To judge the same requests input at the same time, this class use a Vector to collect each request and judge new request at the same time window.
 * @ INHERIT: <Thread> | <public void run()>
 * @ INVARIANT: None;
 */
public class DealSameRequest extends Thread{
	static Vector<Passenger> reqlist = new Vector<Passenger>();
	
	/**
	 * @REQUIRES: None
	 * @MODIFIES: reqlist;
	 * @EFFECTS: 
	 * (reqlist.size()==0) ==> \result == false;
	 * (newp.time==reqlist.get(0).time && reqlist.contains(newp)) ==> reqlist.add(newp);
	 * (newp.time==reqlist.get(0).time && reqlist.contains(newp) && reqlist.size()>300) ==> System.exit(0);
	 * (newp.time==reqlist.get(0).time && reqlist.contains(newp) && reqlist.size()<=300) ==> \result == true;
	 * (newp.time==reqlist.get(0).time && !reqlist.contains(newp)) ==> reqlist.add(newp);
	 * (newp.time==reqlist.get(0).time && !reqlist.contains(newp) && reqlist.size()>300) ==> System.exit(0);
	 * (newp.time==reqlist.get(0).time && !reqlist.contains(newp) && reqlist.size()<=300) ==> \result == false;
	 * (newp.time!=reqlist.get(0)) ==> reqlist.clear();reqlist.add(newp);\result == false;
	 */
	public boolean isfindsame() {
		Passenger newp = reqlist.get(reqlist.size()-1);
		reqlist.remove(reqlist.size()-1);
		if(reqlist.size()==0) {
			reqlist.add(newp);
			return false;
		}
		if(Math.abs(newp.time-reqlist.get(0).time)<100) {
			if(reqlist.contains(newp)) {
//				System.out.println("["+gv.getFormatTime()+"]:"+str+"-Error: Same Request!");
				reqlist.add(newp);
				if(reqlist.size()>300) {
					System.out.println("["+gv.getFormatTime()+"]:Only 300 instruction support at the same time!");
					System.exit(0);
				}
				return true;
			} else {
				reqlist.add(newp);
				if(reqlist.size()>300) {
					System.out.println("["+gv.getFormatTime()+"]:Only 300 instruction support at the same time!");
					System.exit(0);
				}
				return false;
			}
		} else {
			reqlist.clear();
			reqlist.add(newp);
			return false;
		}
	}
	
	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		if(reqlist.size()>301)	return false;
		return true;
	}
	
}
