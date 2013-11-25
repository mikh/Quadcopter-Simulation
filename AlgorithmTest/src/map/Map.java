package map;


import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import defaults.Def;
import gui.GUI;

/**
 * Class that defines the map and is used to maintain both the gui elements, and the quadcopter vision
 * @author Mikhail Andreev
 * @code_description
 * 	 0 - unassigned
 *   1 - Wall
 *   2 - Movable area
 *   3 - person
 *   4 - quadcopter
 *   5 - sensed area
 *   6 - sensed and searched
 *   7 - currently sensing
 *   8 - currently searching
 *   9 - path (sensed and searched)
 * 
 * 
 * TODO: comments and control code
 * TODO: Streamline
 * TODO: Finish Generation code
 */
public class Map {
	
	//maintains a numeric code 2d array
	private ArrayList<ArrayList<Integer>> grid;
	private BufferedWriter log;		//logging 
	private GUI gui;	//used to call update functions
	
	
	/**
	 * Map constructor
	 * Initializes default values. Checks and fits default values so that they work best together
	 * @param log_file 
	 * @throws IOException: If there is an issue with logging file
	 */
	public Map(BufferedWriter log_file) throws IOException{
		grid = new ArrayList<ArrayList<Integer>>();
		log = log_file;
		Def.output(log, "Map construction initiated\r\n");
		//get number of squares
		int xx = Def.MAP_SIZE_FT_X, yy = Def.MAP_SIZE_FT_Y;
		
		if((Def.MAP_SIZE_FT_X % Def.FT_PER_SQUARE) != 0){
			xx += Def.FT_PER_SQUARE - (Def.MAP_SIZE_FT_X % Def.FT_PER_SQUARE);
			log.write("\r\nMap Foot size is not compatible with foot per square size. Changing Map size to " + xx + ". Please note this may create further problems down the road.\r\n");
		}
		if((Def.MAP_SIZE_FT_Y % Def.FT_PER_SQUARE) != 0){
			yy += Def.FT_PER_SQUARE - (Def.MAP_SIZE_FT_Y % Def.FT_PER_SQUARE);
			log.write("\r\nMap Foot size is not compatible with foot per square size. Changing Map size to " + yy + ". Please note this may create further problems down the road.\r\n");

		}	//resize feet to fit map
		int sq_row = xx/Def.FT_PER_SQUARE, sq_col = yy/Def.FT_PER_SQUARE;
		
		Def.output(log, String.format("Found square size in pixels. x = %dpx y = %dpx\r\n", sq_row, sq_col));
		
		//initialize grid to 0
		for(int ii = 0; ii < sq_row; ii++){
			grid.add(new ArrayList<Integer>());
			for(int jj = 0; jj < sq_col; jj++){
				grid.get(ii).add(0);
			}
		}
	}
	
	/**
	 * Loads a premade map. Only certain maps are accepted
	 * @param map - can be Def.SAMPLE_MAP_1
	 * @throws IOException - if the file cannot be opened
	 */
	public void load_map(int map) throws IOException{
		if(map == Def.SAMPLE_MAP_1){
			Def.output(log, "Loading map sample map 1...\r\n");
			
			BufferedReader br = new BufferedReader(new FileReader("sample_map_1.txt"));
			String line = br.readLine();
			int index = 0;
			while(line != null){
				for(int ii = 0; ii < 40; ii++){
					grid.get(index).set(ii, line.charAt(ii) - '0');
				}
				index++;
				if(index == 40)
					break;
				line = br.readLine();
			}
			br.close();
		}
		else{
			Def.output(log, "Map not found.");
			System.exit(-1);
		}
	}
	
	/**
	 * prints out the current statistics based on the map square codes
	 * @throws IOException - if there is an issue with the log file
	 */
	public void print_stats() throws IOException{
		int squares = grid.size() * grid.get(0).size();
		ArrayList<Integer> stats = new ArrayList<Integer>();	//0 = total squares, 1 = black squares, 2 = accessible squares, 3 = squares seen
		stats.add(squares);
		int b_squares = 0, a_squares = 0, s_squares = 0;
		for(int ii = 0; ii < grid.size(); ii++){
			for(int jj = 0; jj < grid.get(ii).size(); jj++){
				if(grid.get(ii).get(jj) == 1)
					b_squares++;
				if(grid.get(ii).get(jj) > 1)
					a_squares++;
				if(grid.get(ii).get(jj) > 2)
					s_squares++;
			}
		}
		stats.add(b_squares);
		stats.add(a_squares);
		stats.add(s_squares);
		
		Def.output(log, "\r\nMap stats: \r\n");
		Def.output(log, String.format("Total squares = %d\nInaccessible squares = %d\nAccessible squares = %d\nAccessed squares = %d\n\n", squares, b_squares, a_squares, s_squares));
	}
	
	public void generate(int seed) throws IOException{
		Def.output(log, "Generating map...\r\n");
		Random rand = new Random(seed);
		
		int grid_x = grid.size(), grid_y = grid.get(0).size();
		int hallL = Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE;
		
		/*******first create border******/
		for(int ii = 0; ii < grid_x; ii++){
			if(ii == 0 || ii == grid_x - 1){
				for(int jj = 0; jj < grid_y; jj++)
					grid.get(ii).set(jj, Def.WALL_CODE);
			}
			else{
				grid.get(ii).set(0, 1);
				grid.get(ii).set(grid_y-1, 1);
			}
		}
		
		/*******generate start position*****/
		int pos;
		switch(rand.nextInt(4)){
			case Def.UP:
				pos = rand.nextInt(grid_x - (hallL - 1)) + (hallL-1)/2;
				for(int ii = pos - (hallL-1)/2; ii <= pos + (hallL-1)/2; ii++)
					grid.get(0).set(ii, Def.MOVABLE_AREA_CODE);
				break;
			case Def.DOWN:
				pos = rand.nextInt(grid_x - (hallL - 1)) + (hallL-1)/2;
				for(int ii = pos - (hallL-1)/2; ii <= pos + (hallL-1)/2; ii++)
					grid.get(grid_x-1).set(ii, Def.MOVABLE_AREA_CODE);
				break;
			case Def.RIGHT:
				pos = rand.nextInt(grid_y - (hallL - 1)) + (hallL-1)/2;
				for(int ii = pos - (hallL-1)/2; ii <= pos + (hallL-1)/2; ii++)
					grid.get(ii).set(grid_y - 1, Def.MOVABLE_AREA_CODE);
				break;
			case Def.LEFT:
				pos = rand.nextInt(grid_y - (hallL - 1)) + (hallL-1)/2;
				for(int ii = pos - (hallL-1)/2; ii <= pos + (hallL-1)/2; ii++)
					grid.get(ii).set(0 , Def.MOVABLE_AREA_CODE);
				break;		
		}
		
		/******generate rooms*****/	
		ArrayList<room> rooms = new ArrayList<room>();
		
		//convert room size in ft into squares
		int min_size = Def.MINIMUM_SIZE_OF_ROOM_FT/Def.FT_PER_SQUARE;
		int max_size = Def.MAXIMUM_SIZE_OF_ROOM_FT/Def.FT_PER_SQUARE;
		
		//generate each room
		for(int ii = 0; ii < Def.NUMBER_OF_ROOMS; ii++){
			Point start = new Point(0,0), end = new Point(0, 0);
			boolean coords_pass = false;
			
			//try coordinates until you get valid ones
			while(!coords_pass){
				//room must be at a minimum 3x3 grid
				int start_x = rand.nextInt(grid_x-4) + 1;
				int start_y = rand.nextInt(grid_y-4) + 1;
			
				int end_x = start_x + (rand.nextInt(max_size-min_size) + min_size);
				int end_y = start_y + (rand.nextInt(max_size-min_size) + min_size);
				if(end_x >= grid_x) end_x = grid_x-2;
				if(end_y >= grid_y) end_y = grid_y-2;
				
				start = new Point(start_x, start_y);
				end = new Point(end_x, end_y);
				Point a = new Point(start_x, end_y);
				Point b = new Point(end_x, start_y);
				
				//check coords
				coords_pass = true;
				for(int jj = 0; jj < rooms.size(); jj++){
					room rm = rooms.get(jj);
					if(!Def.point_within_range(start, rm.start, rm.end, 1)){ coords_pass = false; break;}
					if(!Def.point_within_range(end, rm.start, rm.end, 1)){ coords_pass = false; break;}
					if(!Def.point_within_range(a, rm.start, rm.end, 1)){ coords_pass = false; break;}
					if(!Def.point_within_range(b, rm.start, rm.end, 1)){ coords_pass = false; break;}
					if(!Def.point_within_range(rm.start, start, end, 1)){ coords_pass = false; break;}
					if(!Def.point_within_range(rm.end, start, end, 1)){ coords_pass = false; break;}
					if(!Def.point_within_range(new Point(rm.start.x, rm.end.y), start, end, 1)){ coords_pass = false; break;}
					if(!Def.point_within_range(new Point(rm.end.x, rm.start.y), start, end, 1)){ coords_pass = false; break;}
				}
			}
			
			//if coords pass, make the room
			for(int jj = start.x; jj <= end.x; jj++){
				for(int kk = start.y; kk <= end.y; kk++){
					grid.get(jj).set(kk, Def.MOVABLE_AREA_CODE);
				}
			}
			room new_room = new room(0, start, end);
			rooms.add(new_room);
			
			Def.output(log, String.format("Creating room between (%d,%d) and (%d,%d)\r\n", start.x, start.y, end.x, end.y));
			
		}
		
		
		/****** generate Hallways *****/
		
		
		
	}
	
	/**
	 * gets the value at the specified xx, yy values
	 * @param xx 
	 * @param yy
	 * @return -1 if xx,yy are invalid or the value in the grid
	 */
	public int get(int xx, int yy){
		if(xx < 0 || xx > grid.size())
			return -1;
		else if(yy < 0 || yy > grid.get(xx).size())
			return -1;
		else
			return grid.get(xx).get(yy);
	}
	
	
	/**
	 * Updates the value in the grid of the <xx>, <yy> position to <type>
	 * @param xx
	 * @param yy
	 * @param type
	 * @return -1 if update unsuccesful, or 0 if it is
	 */
	public int update(int xx, int yy, int type){
		if(xx < 0 || xx > grid.size())
			return -1;
		else if(yy < 0 || yy > grid.get(xx).size())
			return -1;
		else if(type < 1 || type > 9)
			return -1;
		else{
			grid.get(xx).set(yy, type);
			gui.update_square(xx, yy);
			return 0;
		}
	}
	
	/**
	 * Finds the starting position of the quadcopter
	 * @return Point where the middle of the hallway is 
	 * @throws IOException  if there is an error with the log file
	 */
	public Point find_start_position() throws IOException{
		int xx = -1, yy = -1;
		for(int ii = 0; ii < grid.size(); ii++){
			if(grid.get(ii).get(0) == 2){
				xx = ii + 1;
				yy = 0;
				break;
			}
			else if(grid.get(ii).get(grid.get(ii).size() - 1) == 2){
				xx = ii + 1;
				yy = grid.get(ii).size() - 1;
				break;
			}
			else if(grid.get(0).get(ii) == 2){
				xx = 0;
				yy = ii + 1;
				break;
			}
			else if(grid.get(grid.size()-1).get(ii) == 2){
				xx = grid.size()-1;
				yy = ii + 1;
				break;
			}
		}
		Def.output(log, String.format("Starting position is at (%d,%d)\r\n", xx, yy));
		return new Point(xx, yy);
	}
	
	/**
	 * Prints a ascii version of the map to map.txt
	 * @throws IOException - if there is an issue with map.txt
	 */
	public void printMap() throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter("map.txt"));
		for(int ii = 0; ii < grid.size(); ii++){
			for(int jj = 0; jj < grid.get(ii).size(); jj++){
				bw.write('0' + grid.get(ii).get(jj));
			}
			bw.write("\r\n");
		}		
		bw.close();
	}
	
	/**
	 * Attaches GUI object to Map object
	 * @param gui
	 */
	public void attachGUI(GUI gui){ this.gui = gui;}
}

/**
 * Hallway struct
 * @author Mikhail
 *
 */
class hallway{
	public Point start;
	public Point end;
	public int direction;	//0 = +y, 1 = -x, 2 = -y, 3 = +x
	
	public hallway(Point start, Point end, int direction){
		this.direction = direction;
		if(start.x < end.x || start.y < end.y){
			this.start = start;
			this.end = end;
		}
		else{
			this.start = end;
			this.end = start;
		}
	}
}

/**
 * room struct
 * @author Mikhail
 *
 */
class room{
	public int doors;
	public Point start;
	public Point end;
	
	public room(int doors, Point start, Point end){
		this.doors = doors;
		this.start = start;
		this.end = end;
	}
}
