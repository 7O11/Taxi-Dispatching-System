package oo_11;

import javax.activity.InvalidActivityException;

/**
 * @ OVERVIEW: record the flow info.
 * @ INVARIANT: None;
 */
public class FlowInfo {
	int count=0;
	long time=0;
	
	/**
	 *@REQUIRES：c>0 && t>=0;
	 *@MODIFIES: this;
  	 *@EFFECTS： (\result = this) && (this.count == c) && (this.time == t);
	*/
	public FlowInfo(int c,long t){
		count = c;
		time = t;
	}
	
	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		if(count<0)	return false;
		if(time<0)	return false;
		return true;
	}
}
