package oo_11;

import javax.activity.InvalidActivityException;

/**
 * @ OVERVIEW: Control every light change their color at the same time.
 * @ INHERIT: <Thread> | <public void run()>
 * @ INVARIANT: None;
 */
public class LightControl extends Thread{
	static Light[][] lightwait = new Light[85][85];
	static long lasttime;
	static long starttime;
	static long rest;
	TaxiGUI gui = new TaxiGUI();
	
	/**
	 * @REQUIRES: None;
	 * @MODIFIES: light.color,LightControl.starttime;
	 * @EFFECTS: 
	 *  (timewindow>=lasttime && (\exist int i,j;0<=i,j<=79 && LightControl.lightwait[i][j].exist)) ==> 
	 * 						light.color = (light.getcolor().equals(LightStatus.NS_RED))?LightStatus.NS_GREEN:LightStatus.NS_RED;
	 * (timewindow>=lasttime) ==> LightControl.starttime = System.currentTimeMillis();
	 * @THREAD_REQUIRES: \this
	 * @THREAD_EFFECTS: \this
	 */
	public void run() {
		try {
			while(true) {
				long timewindow = System.currentTimeMillis() - LightControl.starttime;
				rest = lasttime - (System.currentTimeMillis() - LightControl.starttime);
				if(timewindow>=lasttime) {
					for(int i=0;i<80;i++) {
						for(int j=0;j<80;j++) {
							Light light = LightControl.lightwait[i][j];
							if(light.exist) {
								light.color = (light.getcolor().equals(LightStatus.NS_RED))?LightStatus.NS_GREEN:LightStatus.NS_RED;
								if(light.getcolor().equals(LightStatus.NS_RED)) {
									gui.SetLightStatus(light.getsite(), 1);	
									
								} else if(light.getcolor().equals(LightStatus.NS_GREEN)){
									gui.SetLightStatus(light.getsite(), 2);
								} else {
									System.out.println("Error in LightControl!");
								}
							}
						}
					}
					LightControl.starttime = System.currentTimeMillis();
				}
			}
		} catch(Exception e) {
			System.out.println("LightControl error!");
		}
		
	}
	
	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		if(lasttime<0)	return false;
		if(starttime<0)	return false;
		if(rest<0)	return false;
		return true;
	}
}
