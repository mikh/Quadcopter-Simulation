package defaults;

//TODO: comments and control code
//TODO: Streamline

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Def {
	
	public static final int SEED = 34;
	
	/**** Code Defines ****/
		/* code description:
		 *  0 - unassigned
		 *  1 - Wall
		 *  2 - Movable area
		 *  3 - person
		 *  4 - quadcopter
		 *  5 - sensed area
		 *  6 - sensed and searched
		 *  7 - currently sensing
		 *  8 - currently searching
		 *  9 - path (sensed and searched)
		 */
		public static final int UNASSIGNED_CODE = 0;
		public static final int WALL_CODE = 1;
		public static final int MOVABLE_AREA_CODE = 2;
		public static final int PERSON_CODE = 3;
		public static final int QUADCOPTER_CODE = 4;
		public static final int SENSED_AREA_CODE = 5;
		public static final int SENSED_AND_SEARCHED_AREA_CODE = 6;
		public static final int CURRENTLY_SENSING_AREA_CODE = 7;
		public static final int CURRENTLY_SEARCHING_AREA_CODE = 8;
		public static final int PATH_CODE = 9;
		public static final int CUSTOM_COLOR_1 = 10;
		public static final int CUSTOM_COLOR_2 = 11;
		
	/** End Code Defines **/
	
	/** Directional Defines **/
		public static final int UP = 0;
		public static final int RIGHT = 1;
		public static final int DOWN = 2;
		public static final int LEFT = 3;
	/** End Directional defines **/
	
		
	/** Sensor Map Defines **/
		public static final int SENSOR_MAP_SIZE_X = 90;
		public static final int SENSOR_MAP_SIZE_Y = 90;
		public static final Point SENSOR_MAP_QUADCOPTER_POSITION = new Point(22, 22);
	/** End Sensor Map Defines **/
		
	/** Dynamic Map Defines **/
		public static final int DYNAMIC_MAP_INITIAL_SIZE_X = 50;
		public static final int DYNAMIC_MAP_INITIAL_SIZE_Y = 50;
	/** End Dynamic Map Defines **/
		
	/** Sensor Defines **/
		public static final double SONAR_SENSOR_CUTOFF = 6.01;
		public static final double SONAR_SENSOR_MAX_DISTANCE = 8.00;
		public static final double SONAR_SENSOR_MIN_DISTANCE = 0.50;
		public static final double SONAR_SENSOR_RANGING_DELAY = 200; //200us
		
		public static final double LASER_SENSOR_CUTOFF = 40.00;
		public static final double LASER_SENSOR_MAX_DISTANCE = 41.00;
		public static final double LASER_SENSOR_MIN_DISTANCE = 0.50;
		public static final double LASER_SENSOR_RANGING_DELAY = 200; //200 us
	/** End Sensor Defines **/
		
	/** GUI Defines **/
		public static final String LOG_FILE_PATH = "log.txt";
		public static final int FRAME_SIZE_X = 450;
		public static final int FRAME_SIZE_Y = 450;
		public static final int FRAME_POSITION_X = 50;
		public static final int FRAME_POSITION_Y = 50;
		public static final int MAP_SIZE_PIXELS_X = 400;
		public static final int MAP_SIZE_PIXELS_Y = 400;
		public static final int MAP_SIZE_FT_X = 200;
		public static final int MAP_SIZE_FT_Y = 200;
		public static final int FT_PER_SQUARE = 2;
		
		public static final int FRAME_DYNAMIC_SIZE_X = 450;
		public static final int FRAME_DYNAMIC_SIZE_Y = 450;
		public static final int FRAME_DYNAMIC_POSITION_X = 900;
		public static final int FRAME_DYNAMIC_POSITION_Y = 50;
		
		public static final int FRAME_SENSOR_SIZE_X = 300;
		public static final int FRAME_SENSOR_SIZE_Y = 300;
		public static final int FRAME_SENSOR_POSITION_X = 550;
		public static final int FRAME_SENSOR_POSITION_Y = 50;
		
		
		public static final int FRAME_STATS_SIZE_X = 300;
		public static final int FRAME_STATS_SIZE_Y = 400;
		public static final int FRAME_STATS_POSITION_X = 550;
		public static final int FRAME_STATS_POSITION_Y = 400;

		public static final Color FRAME_COLOR = new Color(5, 5, 5, 100);
			
		public static final Color WALL_COLOR = new Color(0,0,0);		//black
		public static final Color MOVABLE_AREA_COLOR = new Color(72,209,204);	//mediumturquoise
		public static final Color PERSON_COLOR = new Color(255,69,0);	//orangered1
		public static final Color QUADCOPTER_LOCATION_COLOR = new Color(0, 238, 0);	//green2
		public static final Color SENSED_AREA_COLOR = new Color(3, 168, 158);	//manganeseblue
		public static final Color SENSED_AND_SEARCHED_AREA_COLOR = new Color(0, 104, 139);	//deepskyblue4
		public static final Color /* CURRENTLY_SENSING_AREA_COLOR = new Color(135, 206, 255);	//skyblue1*/ CURRENTLY_SENSING_AREA_COLOR = new Color(0, 104, 139);	//deepskyblue4
		public static final Color CURRENTLY_SEARCHING_AREA_COLOR = new Color(255, 255, 0);	//yellow
		public static final Color PATH_COLOR = new Color(0, 0, 255);		//blue1
		public static final Color CUSTOM_COLOR = new Color(255, 0, 0);		//red1
	/** End GUI defines **/
	
	
	/** Generation Defines **/
		public static final int HALLWAY_LENGTH_FT = 6;	//if this isn't a multiple of ft/square then crash
		public static final int NUMBER_OF_ROOMS = 8;
		public static final int NUMBER_OF_SURVIVORS = 10;
		public static final int MINIMUM_SIZE_OF_ROOM_FT = 20;		//has to match with ft/square
		public static final int MAXIMUM_SIZE_OF_ROOM_FT = 40;		//has to match with ft/sq
		public static final int MAXIMUM_NUMBER_OF_DOORS_PER_ROOM = 2;
		public static final int DOOR_SIZE_FT = 4;
		public static final int HALLWAY_INCREMENTS = 1000;
		public static final int ROOM_CONNECTIONS_MIN = 2;
		public static final int ROOM_CONNECTIONS_MAX = 3;
		
		public static final double PROBABILITY_OF_ROOM = 0.1;
		public static final double PROBABILITY_OF_FORK = 0.05;
		public static final double PROBABILITY_OF_TURN = 0.05;
		public static final double PROBABILITY_OF_DEADEND = 0.01;
		
		public static final String SAMPLE_MAP_1 = "sample_map_1.txt";
		public static final String SAMPLE_MAP_2 = "sample_map_2.txt";
	
		public static final int TRY_ATTEMPTS = 100;
		
		//Quadcopter
		public static final int NUMBER_OF_SENSORS = 16;		//can be 8 or 16
		public static final int SENSOR_RANGE_FT = 8;		//anything else may have undefined results
		
		public static final int INTERNAL_VIEW_X_PX = 200;
		public static final int INTERNAL_VIEW_Y_PX = 200;
	/** End Generation Defines **/

	
	//sensor macros
	public static Point SENSOR_SCAN(Point base, int distance, int sensor){
		switch(sensor){
			case 1: return new Point(base.x + (int)(round((double)(0*distance))), base.y + (int)(-1*round((double)(1*distance)))); 
			case 2: return new Point(base.x + (int)(round((double)(1*distance))), base.y + (int)(round((double)(0*distance)))); 
			case 3: return new Point(base.x + (int)(round((double)(0*distance))), base.y + (int)(round((double)(1*distance))));  
			case 4: return new Point(base.x + (int)(-1*round((double)(1*distance))), base.y + (int)(round((double)(0*distance))));  
			case 5: return new Point(base.x + (int)(-1*round((double)(1*distance))), base.y + (int)(-1*round((double)(1*distance))));  
			case 6: return new Point(base.x + (int)(round((double)(1*distance))), base.y + (int)(-1*round((double)(1*distance))));  
			case 7: return new Point(base.x + (int)(round((double)(1*distance))), base.y + (int)(round((double)(1*distance))));  
			case 8: return new Point(base.x + (int)(-1*round((double)(1*distance))), base.y + (int)(round((double)(1*distance))));  
			case 9: return new Point(base.x + (int)(-1*round((double)(0.38*distance))), base.y + (int)(-1*round((double)(0.92*distance))));  
			case 10: return new Point(base.x + (int)(1*round((double)(0.38*distance))), base.y + (int)(-1*round((double)(0.92*distance))));  
			case 11: return new Point(base.x + (int)(1*round((double)(0.92*distance))), base.y + (int)(-1*round((double)(0.38*distance))));  
			case 12: return new Point(base.x + (int)(1*round((double)(0.92*distance))), base.y + (int)(1*round((double)(0.38*distance))));  
			case 13: return new Point(base.x + (int)(1*round((double)(0.38*distance))), base.y + (int)(1*round((double)(0.92*distance))));  
			case 14: return new Point(base.x + (int)(-1*round((double)(0.38*distance))), base.y + (int)(1*round((double)(0.92*distance))));  
			case 15: return new Point(base.x + (int)(-1*round((double)(0.92*distance))), base.y + (int)(1*round((double)(0.38*distance))));  
			case 16: return new Point(base.x + (int)(-1*round((double)(0.92*distance))), base.y + (int)(-1*round((double)(0.38*distance))));  
			default: return new Point(-1,-1);
		}
	}

	//round function
	public static int round(double val){ return ((val%1) >= 0.50) ? (int)(Math.ceil(val)) : (int)(Math.floor(val));}
	
	public static void output(BufferedWriter bw, String str) throws IOException{ bw.write(str); System.out.print(str); }

	public static boolean point_within_range(Point x, Point start, Point end, int offset){ if((x.x >= start.x - offset && x.x <= end.x + offset) && (x.y >= start.y - offset && x.y <= end.y + offset)) return false; return true;}

	public static double get_distance(Point a, Point b){return Math.sqrt(Math.pow((double)(Math.abs(a.x - b.x)), 2.0) + Math.pow((double)(Math.abs(a.y-b.y)), 2));}

	public static boolean directions_opposite(int dir1, int dir2){if((dir1 == Def.UP && dir2 == Def.DOWN) || (dir2 == Def.UP && dir1 == Def.DOWN) || (dir1 == Def.RIGHT && dir2 == Def.LEFT) || (dir2 == Def.RIGHT && dir1 == Def.LEFT)) return true; return false;}

	public static Point getNextPoint(Point pt, int direction){if(direction == UP) return new Point(pt.x, pt.y-1); else if(direction == DOWN) return new Point(pt.x, pt.y + 1); else if(direction == RIGHT) return new Point(pt.x+1, pt.y); else if(direction == LEFT) return new Point(pt.x-1, pt.y); return new Point(-1,-1);}

	public static int min(int a, int b){if(a > b) return b; return a;}
	public static int min(int a, int b, int c){return min(min(a,b), c);}
	public static int min(int a, int b, int c, int d){return min(min(a,b),min(c,d));}
	
	public static int max(int a, int b){if(a > b) return a; return b;}
	public static int max(int a, int b, int c){return max(max(a,b),c);}
	public static int max(int a, int b, int c, int d){return max(max(a,b),max(c,d));}
	
	public static String print_point(Point pt){ return "(" + pt.x + "," + pt.y + ")";}
	
	public static void print_arrayList(ArrayList<ArrayList<Integer>> aL){
		System.out.println(" ");
		for(int ii = 0; ii < aL.size(); ii++){
			for(int jj = 0; jj < aL.get(ii).size(); jj++){
				System.out.print(" " + aL.get(ii).get(jj));
			}
			System.out.println(" ");
		}
		System.out.println(" ");
	}
	
	public static Point convertDistanceToGridPosition(Point start, double distance, double angle, double ft_per_square){
		if(angle > 360) angle -= 360;
		int direction_x = 1, direction_y = 1;
		if(angle > 90 && angle < 270)
			direction_x = -1;
		if(angle > 0 && angle < 180)
			direction_y = -1;
		return new Point(start.x + (int)(direction_x*distance*Math.abs(Math.cos(angle/360.0*2*Math.PI))/ft_per_square), start.y + (int)(direction_y*distance*Math.abs(Math.sin(angle/360.0*2*Math.PI))/ft_per_square));
	}
	
	public static void delay(long milliseconds){ long start_time = System.currentTimeMillis(); while(System.currentTimeMillis() - start_time < milliseconds);}
}
