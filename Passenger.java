package oo_11;

import javax.activity.InvalidActivityException;

/**
 * @ OVERVIEW: Record the detail of each request, including beginning time and site and etc.
 * @ INVARIANT: 
 * 		0<=srcx<=79;
 * 		0<=srcy<=79;
 * 		0<=dstx<=79;
 * 		0<=dsty<=79;
 */
public class Passenger{
	int numcount;
	long time;
	int srcx=0,srcy=0;
	int dstx=0,dsty=1;
	
	/**
	 * @REQUIRES: None;
	 * @MODIFIES: None;
	 * @EFFECTS: None;
	 */
	public Passenger() {}
	
	/**
	 * @REQUIRES: 0<=x0<=79 && 0<=y0<=79 && 0<=x1<=79 && 0<=y1<=79 && t>=0;
	 * @MODIFIES: srcx,srcy,dstx,dsty,time;
	 * @EFFECTS: \result == this && this.srcx == x0 && this.srcy == y0 && this.dstx == x1 && this.dsty == y1 && this.time == t;
	 */
	public Passenger(int x0,int y0,int x1,int y1,long t) {
		srcx = x0;
		srcy = y0;
		dstx = x1;
		dsty = y1;
		time = t;
	}
	
	/**
	 * get string
	 * @REQUIRES: None
	 * @MODIFIES: None
	 * @EFFECTS: \result == "[CR,("+srcx+","+srcy+"),("+dstx+","+dsty+")]";
	 */
	public String getrequest() {
		String string = "";
		string = "[CR,("+srcx+","+srcy+"),("+dstx+","+dsty+")]"; 
		return string;
	}
	
	/**
	 * @REQUIRES: None
	 * @MODIFIES: None
	 * @EFFECTS: \result == "CreateTime:"+time+"; Start:("+srcx+","+srcy+"); Dst:"+"("+dstx+","+dsty+")";
	 */
	public String getreqInfo() {
		String string = "CreateTime:"+time+"; Start:("+srcx+","+srcy+"); Dst:"+"("+dstx+","+dsty+")";
		return string;
	}
	
	/**
	 * @REQUIRES: None
	 * @MODIFIES: None
	 * @EFFECTS: 
	 * (object instanceof Passenger) ==> \result == this.srcx==passenger.srcx && this.srcy==passenger.srcy && this.dstx==passenger.dstx && this.dsty==passenger.dsty;
	 * !(object instanceof Passenger) ==> super.equals(object);
	 */
	public boolean equals(Object object) {
		if (object instanceof Passenger){
            Passenger passenger = (Passenger)object;
            return this.srcx==passenger.srcx && this.srcy==passenger.srcy && this.dstx==passenger.dstx && this.dsty==passenger.dsty;
        }
		return super.equals(object);
	}

	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		if(numcount<0)	return false;
		if(time<0)	return false;
		if(srcx<0 || srcx>79)	return false;
		if(srcy<0 || srcy>79)	return false;
		if(dstx<0 || dstx>79)	return false;
		if(dsty<0 || dsty>79)	return false;
		return true;
	}
}
