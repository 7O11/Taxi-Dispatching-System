package oo_11;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
//import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.activity.InvalidActivityException;

/**
 * @ OVERVIEW: some action for taxi and map and etc. initialization.
 * @ INVARIANT: None;
 */
public class TaxiMain {
	String line;
	static int[][] map = new int[80][80];
	static int[][] map0 = new int[80][80];
	public static int SPECIALNUM = 30;
	static CopyOnWriteArrayList<Taxi> taxis = new CopyOnWriteArrayList<Taxi>();
	
	/**
	 * update the map in TaxiMain
	 * @REQUIRES: status==0 || status==1;
	 * @MODIFIES: TaxiMain.map, TaxiMain.map0;
	 * @EFFECTS: 
	 * 	(di==0 && (dj==1 || dj==-1) && status==0 && map[p.x][p.y]==3) ==> map[p.x][p.y] = 2;
	 * 	(di==0 && (dj==1 || dj==-1) && status==0 && map[p.x][p.y]==1) ==> map[p.x][p.y] = 0;
	 *  (di==0 && (dj==1 || dj==-1) && status==1 && map[p.x][p.y]==2) ==> map[p.x][p.y] = 3;
	 *  (di==0 && (dj==1 || dj==-1) && status==1 && map[p.x][p.y]==0) ==> map[p.x][p.y] = 3;
	 *  (dj==0 && (di==1 || di==-1) && status==0 && map[p.x][p.y]==3) ==> map[p.x][p.y] = 1;
	 * 	(dj==0 && (di==1 || di==-1) && status==0 && map[p.x][p.y]==2) ==> map[p.x][p.y] = 0;
	 *  (dj==0 && (di==1 || di==-1) && status==1 && map[p.x][p.y]==1) ==> map[p.x][p.y] = 3;
	 *  (dj==0 && (di==1 || di==-1) && status==1 && map[p.x][p.y]==0) ==> map[p.x][p.y] = 2;
	 */
	public void setRoadStatus(Point p1, Point p2, int status) {// status 0关闭 1打开
		synchronized(map){
			int di = p1.x - p2.x;
			int dj = p1.y - p2.y;
			Point p = null;
			if (di == 0) {// 在同一水平线上
				if (dj == 1) {// p2-p1
					p = p2;
				} else if (dj == -1) {// p1-p2
					p = p1;
				} else {
					return;
				}
				if (status == 0) {// 关闭
					if (map[p.x][p.y] == 3) {
						map[p.x][p.y] = 2;
					} else if (map[p.x][p.y] == 1) {
						map[p.x][p.y] = 0;
					}
				} else if (status == 1) {// 打开
					if (map[p.x][p.y] == 2) {
						map[p.x][p.y] = 3;
					} else if (map[p.x][p.y] == 0) {
						map[p.x][p.y] = 1;
					}
				}
			} else if (dj == 0) {// 在同一竖直线上
				if (di == 1) {// p2-p1
					p = p2;
				} else if (di == -1) {// p1-p2
					p = p1;
				} else {
					return;
				}
				if (status == 0) {// 关闭
					if (map[p.x][p.y] == 3) {
						map[p.x][p.y] = 1;
					} else if (map[p.x][p.y] == 2) {
						map[p.x][p.y] = 0;
					}
				} else if (status == 1) {// 打开
					if (map[p.x][p.y] == 1) {
						map[p.x][p.y] = 3;
					} else if (map[p.x][p.y] == 0) {
						map[p.x][p.y] = 2;
					}
				}
			}
			return;
		}
	}
	
	/**
	 * Judge if we can find correct file and check the format of the file.
	 * @REQUIRES: None;
	 * @MODIFIES: map, map0;
	 * @EFFECTS: 
	 * 	map[i0][j0] = m[i0][j0];
		map0[i0][j0] = m[i0][j0];
	 * 	\result == (new File(filename).exist() && line.correct());
	 */
	public boolean isFileMatch(String filename) {
		int[][] m = new int[85][85];
		int j;
		FileInputStream fi = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		/***************************************Check File Exist*****************************************/
		try {
			fi = new FileInputStream(filename);
			isr = new InputStreamReader(fi);
			br = new BufferedReader(isr);	
		} catch (FileNotFoundException e) {
			return false;
		}

		/***************************************Check Format of Map*****************************************/		
		try {
			int xcount=0;
			while((line=br.readLine())!=null) {
				line.replaceAll(" ", "");
				line.replaceAll("\r", "");
				for(j=0;j<line.length();j++) {
					char c = line.charAt(j);
					if(c!='0' && c!='1' && c!='2' && c!='3') {
						br.close();
						isr.close();
						fi.close();
						return false;
					}
					m[xcount][j] = Integer.parseInt(String.valueOf(c));
				}
				if(j!=80) {
					br.close();
					isr.close();
					fi.close();
					return false;
				}
				xcount++;
			}
			if(xcount!=80) {
				br.close();
				isr.close();
				fi.close();
				return false;
			}
			
		} catch (IOException e1) {
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
		for(int i0=0;i0<80;i0++) {
			for(int j0=0;j0<80;j0++) {
				map[i0][j0] = m[i0][j0];
				map0[i0][j0] = m[i0][j0];
			}
		}
		return true;
	}
	
	/**
	 * @EFFECTS: \result == invariant(this);
	 */
	public boolean repOK() throws InvalidActivityException{
		for(int i=0;i<80;i++) {
			for(int j=0;j<80;j++) {
				if(map[i][j]!=0 && map[i][j]!=1 && map[i][j]!=2 && map[i][j]!=3) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * main
	 * @REQUIRES: None;
	 * @MODIFIES: None;
	 * @EFFECTS: \result == this;
	 */
	public static void main(String[] args) {
		try {
			String originfilename = "map.txt";
			TaxiMain taxiMain = new TaxiMain();
			/***************************************Record File Create*****************************************/
			File orderfile = new File("detail.txt");
			if(orderfile.exists()) {
				orderfile.delete();
				try {
					orderfile.createNewFile();	
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					orderfile.createNewFile();	
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(!taxiMain.isFileMatch(originfilename)) {
				System.out.println("["+gv.getFormatTime()+"]:"+"map.txt Error!");
				System.exit(0);
			}

			TaxiGUI gui=new TaxiGUI();
			gui.LoadMap(TaxiMain.map, 80);
			gv.stay(500);
			/**************************************SpecialTaxi run********************************************/
			for(int i=0;i<30;i++) {
				Random random = new Random();
				int x = random.nextInt(80);
				int y = random.nextInt(80);
				SpecialTaxi t = new SpecialTaxi(i, x, y, gui);
				TaxiMain.taxis.add(t);
				gui.SetTaxiType(i, 1);
				t.start();
			}
			/**************************************Taxi run********************************************/
			for(int i=30;i<100;i++) {
				Random random = new Random();
				int x = random.nextInt(80);
				int y = random.nextInt(80);
				Taxi t = new Taxi(i, x, y, gui);
				TaxiMain.taxis.add(t);
				gui.SetTaxiType(i, 0);
				t.start();
			}
			RequestsGet requestsGet = new RequestsGet(gui,taxiMain);
			requestsGet.start();	
		}catch(Exception e) {
			System.out.println("main error!");
		}	
	}

}
