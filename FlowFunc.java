package oo_11;

import java.util.HashMap;
import java.util.Vector;

import javax.activity.InvalidActivityException;

/**
 * @ OVERVIEW: Count the flow from last 500ms to now, and can add real-time new flow, and clear the flow of one road instantly after the road closed. 
 * @ INVARIANT: None;
 */
public class FlowFunc {
	public static HashMap<String,Vector<FlowInfo>> flowmap = new HashMap<String,Vector<FlowInfo>>();//当前流量
	
	/**
	 * @REQUIRES: 0<=x1<=79 && 0<=y1<=79 && 0<=x2<=79 && 0<=y2<=79;
	 * @MODIFIES: None;
	 * @EFFECTS: \result == ""+x1+","+y1+","+x2+","+y2;
	 */
	private static String Key(int x1,int y1,int x2,int y2){//生成唯一的Key
		return ""+x1+","+y1+","+x2+","+y2;
	}
	
	/**
	 * @REQUIRES: flow>0 && 0<=x1<=79 && 0<=y1<=79 && 0<=x2<=79 && 0<=y2<=79;
	 * @MODIFIES: FlowFunc.flowmap;
	 * @EFFECTS: 
	 * (FlowFunc.flowmap.get(Key(x1,y1,x2,y2))==null) ==> FlowFunc.flowmap.put(Key(x1,y1,x2,y2),new Vector<FlowInfo>().add(new FlowInfo(flow,flowtime));
	 * 														FlowFunc.flowmap.put(Key(x2,y2,x1,y1), new Vector<FlowInfo>().add(new FlowInfo(flow,flowtime));
	 * (FlowFunc.flowmap.get(Key(x1,y1,x2,y2))!=null) ==> FlowFunc.flowmap.put(Key(x1,y1,x2,y2), FlowFunc.flowmap.get(Key(x1,y1,x2,y2)).add(new FlowInfo(flow, flowtime)));
	 * 														FlowFunc.flowmap.put(Key(x2,y2,x1,y1), FlowFunc.flowmap.get(Key(x1,y1,x2,y2)).add(new FlowInfo(flow, flowtime)));
	 */
	public static void AddFlow(int x1,int y1,int x2,int y2,int flow){//增加一个道路流量
		long flowtime = System.currentTimeMillis();
		synchronized (FlowFunc.flowmap) {
			if(FlowFunc.flowmap.get(Key(x1,y1,x2,y2))==null) {
				Vector<FlowInfo> oneflow = new Vector<FlowInfo>();
				oneflow.add(new FlowInfo(flow, flowtime));
				FlowFunc.flowmap.put(Key(x1,y1,x2,y2), oneflow);
				FlowFunc.flowmap.put(Key(x2,y2,x1,y1), oneflow);
			} else {
				Vector<FlowInfo> note = FlowFunc.flowmap.get(Key(x1,y1,x2,y2));
				note.add(new FlowInfo(flow, flowtime));
				FlowFunc.flowmap.put(Key(x1,y1,x2,y2), note);
				FlowFunc.flowmap.put(Key(x2,y2,x1,y1), note);
			}
		}
	}
	
	/**
	 * @REQUIRES: 0<=x1<=79 && 0<=y1<=79 && 0<=x2<=79 && 0<=y2<=79;
	 * @MODIFIES: FlowFunc.flowmap;
	 * @EFFECTS: 
	 * (\all i,0<=i<FlowFunc.flowmap.get(Key(x1,y1,x2,y2)).size();(System.currentTimeMillis()-FlowFunc.flowmap.get(Key(x1,y1,x2,y2)).get(i).time)>520) ==> 
	 * 												FlowFunc.flowmap.get(Key(x1,y1,x2,y2)).remove(i);
	 * 												\old(FlowFunc.flowmap.get(Key(x1,y1,x2,y2)).size())!=FlowFunc.flowmap.get(Key(x1,y1,x2,y2)).size();
	 * \result == flowcount;
	 */
	public static int GetFlow(int x1,int y1,int x2,int y2){//查询流量信息
		synchronized(FlowFunc.flowmap) {
			if(FlowFunc.flowmap.get(Key(x1,y1,x2,y2))==null) {
				return 0;
			}
			int flowcount = 0;
			Vector<FlowInfo> note = FlowFunc.flowmap.get(Key(x1,y1,x2,y2));
			for(int i=0;i<note.size();i++) {
				FlowInfo flowInfo = note.get(i);
				if((System.currentTimeMillis()-flowInfo.time)>520) {
//					System.out.println("Systime="+System.currentTimeMillis()+",flowinfotime="+flowInfo.time);
					note.remove(i);
					if(note.size()==0) {
						break;
					}
					i--;
				} else {
					break;
				}
			}
			FlowFunc.flowmap.put(Key(x1,y1,x2,y2), note);
			FlowFunc.flowmap.put(Key(x2,y2,x1,y1), note);
			for(int i=0;i<note.size();i++) {
				FlowInfo flowInfo = note.get(i);
				flowcount += flowInfo.count;
			}
			return flowcount;
		}
	}
	
	/**
	 * @REQUIRES: 0<=x1<=79 && 0<=y1<=79 && 0<=x2<=79 && 0<=y2<=79;
	 * @MODIFIES: FlowFunc.flowmap;
	 * @EFFECTS: 
	 * 	FlowFunc.flowmap.put(Key(x1, y1, x2, y2), null);
	 * 	FlowFunc.flowmap.put(Key(x2, y2, x1, y1), null);
	 */
	public static void ClearFlow(int x1,int y1,int x2,int y2) {
		synchronized(FlowFunc.flowmap) {
			FlowFunc.flowmap.put(Key(x1, y1, x2, y2), null);
			FlowFunc.flowmap.put(Key(x2, y2, x1, y1), null);
		}
	}
	
	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		for(int i=0;i<80;i++) {
			for(int j=0;j<80;j++) {
				if(i!=79) {
					if(GetFlow(i, j, i+1, j)>100)	return false;
				}
				if(j!=79) {
					if(GetFlow(i, j, i, j+1)>100)	return false;
				}
			}
		}
		return true;
	}
	
}
