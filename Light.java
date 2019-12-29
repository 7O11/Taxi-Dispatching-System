package oo_11;

import java.awt.Point;

import javax.activity.InvalidActivityException;

enum LightStatus{
	NS_RED,NS_GREEN;
}

/**
 * @ OVERVIEW: record the info of each light.
 * @ INVARIANT: None;
 */
public class Light {
	private int x;
	private int y;
	boolean exist=false;
	LightStatus color;
	
	/**
	 *@REQUIRES：None;
	 *@MODIFIES: None;
  	 *@EFFECTS： None;
	*/
	Light(){}
	
	/**
	 *@REQUIRES：0<=x0<=79 && 0<=y0<=79;
	 *@MODIFIES: this;
  	 *@EFFECTS： (\result = this) && (this.x == x0) && (this.y == y0);
	*/
	Light(int x0,int y0){
		x = x0;
		y = y0;
	}
	
	/**
	 *@REQUIRES：0<=x<=79 && 0<=y<=79;
	 *@MODIFIES: None;
  	 *@EFFECTS： \result == new Point(x, y);
	*/
	public Point getsite() {
		return new Point(x, y);
	}
	
	/**
	 *@REQUIRES：None;
	 *@MODIFIES: None;
  	 *@EFFECTS： \result == this.color;
	*/
	public synchronized LightStatus getcolor() {
		return this.color;
	}
	
	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		if(x<0 || x>79) return false;
		if(y<0 || y>79)	return false;
		return true;
	}
	
}
