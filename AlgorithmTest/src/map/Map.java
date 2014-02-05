package map;


import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
		Point start_pos_start = new Point(0,0), start_pos_end = new Point(0,0);
		switch(rand.nextInt(4)){
			case Def.UP:
				pos = rand.nextInt(grid_x - (hallL - 1)) + (hallL-1)/2;
				start_pos_start = new Point(0, pos-(hallL-1)/2-1);
				start_pos_end = new Point(0, pos + (hallL-1)/2+1);
				for(int ii = pos - (hallL-1)/2-1; ii <= pos + (hallL-1)/2+1; ii++)
					grid.get(0).set(ii, Def.MOVABLE_AREA_CODE);
				break;
			case Def.DOWN:
				pos = rand.nextInt(grid_x - (hallL - 1)) + (hallL-1)/2;
				start_pos_start = new Point(grid_x-1, pos-(hallL-1)/2-1);
				start_pos_end = new Point(grid_x-1, pos+(hallL-1)/2+1);
				for(int ii = pos - (hallL-1)/2-1; ii <= pos + (hallL-1)/2+1; ii++)
					grid.get(grid_x-1).set(ii, Def.MOVABLE_AREA_CODE);
				break;
			case Def.RIGHT:
				pos = rand.nextInt(grid_y - (hallL - 1)) + (hallL-1)/2;
				start_pos_start = new Point(pos - (hallL-1)/2-1, grid_y-1);
				start_pos_end = new Point(pos + (hallL-1)/2+1, grid_y-1);
				for(int ii = pos - (hallL-1)/2-1; ii <= pos + (hallL-1)/2+1; ii++)
					grid.get(ii).set(grid_y - 1, Def.MOVABLE_AREA_CODE);
				break;
			case Def.LEFT:
				pos = rand.nextInt(grid_y - (hallL - 1)) + (hallL-1)/2;
				start_pos_start= new Point(pos - (hallL-1)/2-1, 0);
				start_pos_end = new Point(pos + (hallL-1)/2+1, 0);
				for(int ii = pos - (hallL-1)/2-1; ii <= pos + (hallL-1)/2+1; ii++)
					grid.get(ii).set(0 , Def.MOVABLE_AREA_CODE);
				break;		
		}
		room start_position_room = new room(0, 1, start_pos_start, start_pos_end, this);
		
		/******generate rooms*****/	
		ArrayList<room> rooms = new ArrayList<room>();
		rooms.add(start_position_room);
		
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
			room new_room = new room(ii+1, 0, start, end, this);
			rooms.add(new_room);
			
			Def.output(log, String.format("Creating room between (%d,%d) and (%d,%d)\r\n", start.x, start.y, end.x, end.y));
			
		}
		
		
		/****** generate Hallways *******/
		
		int room1 = 0;
		rooms.get(room1).color(10);
		HashMap<Integer, Boolean> exclusions = new HashMap<Integer, Boolean>();
		int room2 = find_closest_room(rooms, room1, exclusions);
		//rooms.get(room2).color(10);
		System.out.println("Closest room to room " + room1 + " defined between (" + rooms.get(room1).start.x + "," + rooms.get(room1).start.y + ") and (" + rooms.get(room1).end.x + "," + rooms.get(room1).end.y + ") is room " + room2 + " defined between (" + rooms.get(room2).start.x + "," + rooms.get(room2).start.y + ") and (" + rooms.get(room2).end.x + "," + rooms.get(room2).end.y);
		ArrayList<Point> faces = find_opposing_faces(rooms, room1, room2);
		//color(faces.get(0), faces.get(1), Def.CUSTOM_COLOR_2);
		//color(faces.get(2), faces.get(3), Def.CUSTOM_COLOR_2);
		System.out.println("The opposing faces of the rooms are between (" + faces.get(0).x + "," + faces.get(0).y + "), (" + faces.get(1).x + "," + faces.get(1).y + ") for room " + room1 + " and (" + faces.get(2).x + "," + faces.get(2).y + "), (" + faces.get(3).x + "," + faces.get(3).y + ") for room " + room2);
		ArrayList<Point> qq = generateEntrancePoints(seed, rooms, room1, room2, faces);
		if(qq.size() == 1){
			if(qq.get(0).x == 0){
				System.out.println("Error! room " + room1 + " can't be connected to. Make something...");
				while(true);
			} else{
				exclusions.put(qq.get(1).x, true);
				room2 = find_closest_room(rooms, room1, exclusions);
				if(room2 == -1){
					System.out.println("Error! room " + room1 + " can't be connected to. Make something...");
					while(true);
				}
			}
		}
		pathing(seed, rooms, room1, room2, qq, new Point(-1,-1), new Point(-1,-1));
		
	
	}
	
	private void pathing(int seed, ArrayList<room> rooms, int rm_1, int rm_2, ArrayList<Point> entrances, Point start_map_size, Point end_map_size){
		Random rand = new Random(seed);
		room rm1 = rooms.get(rm_1), rm2 = rooms.get(rm_2);
		int hallL = Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE, grid_x = grid.size(), grid_y = grid.get(0).size();
		
		//if map_size isn't defined
		if(start_map_size.x == -1){
			//find all points
			Point p1_s = rm1.start, p1_e = rm1.end, p2_s = rm2.start, p2_e = rm2.end;
			int min_x = Def.min(p1_s.x, p1_e.x, p2_s.x, p2_e.x);
			int min_y = Def.min(p1_s.y, p1_e.y, p2_s.y, p2_e.y);
			int max_x = Def.max(p1_s.x, p1_e.x, p2_s.x, p2_e.x);
			int max_y = Def.max(p1_s.y, p1_e.y, p2_s.y, p2_e.y);
			
			start_map_size = new Point(min_x, min_y);
			end_map_size = new Point(max_x, max_y);
		}
		
		//create partial map
		int pm_x = end_map_size.x - start_map_size.x + 1, pm_y = end_map_size.y - start_map_size.y + 1;
		ArrayList<ArrayList<Integer>> partial_map = new ArrayList<ArrayList<Integer>>();
		for(int ii = 0; ii < pm_x; ii++){
			ArrayList<Integer> row = new ArrayList<Integer>();
			for(int jj = 0; jj < pm_y; jj++){
				row.add(0);
			}
			partial_map.add(row);
		}
		//System.out.println("Creating partial map based on points " + Def.print_point(start_map_size) + " and " + Def.print_point(end_map_size) + " with dimensions " + pm_x + "x" + pm_y);
		
		//populate partial map
		//System.out.println(" ");
		for(int ii = 0; ii < pm_x; ii++){
			for(int jj = 0; jj < pm_y; jj++){
				if(grid.get(start_map_size.x+ii).get(start_map_size.y+jj) != 0)
					partial_map.get(ii).set(jj, 1);
				else
					partial_map.get(ii).set(jj, 0);
				//System.out.print(" " + partial_map.get(ii).get(jj));
			}
			//System.out.println(" ");
		}
		
		//add entrance point
		Point e_s, e_e;
		Point start_point = new Point(-1,-1), end_point = new Point(-1,-1);
		for(int ii = 0; ii < 2; ii++){
			e_s = new Point(Def.min(entrances.get(ii*2).x, entrances.get(ii*2+1).x), Def.min(entrances.get(ii*2).y, entrances.get(ii*2+1).y));
			e_e = new Point(Def.max(entrances.get(ii*2).x, entrances.get(ii*2+1).x), Def.max(entrances.get(ii*2).y, entrances.get(ii*2+1).y));
			System.out.println(Def.print_point(e_s) + " " + Def.print_point(e_e));
			for(int jj = e_s.x; jj <= e_e.x; jj++){
				for(int kk = e_s.y; kk <= e_e.y; kk++){
					partial_map.get(jj-start_map_size.x).set(kk-start_map_size.y, 1);
					if(jj == (int)Math.ceil((e_s.x + e_e.x)/2) && kk == (int)Math.ceil((e_s.y+e_e.y)/2)){
						partial_map.get(jj-start_map_size.x).set(kk-start_map_size.y, 2);
						if(start_point.x == -1)
							start_point = new Point(jj - start_map_size.x, kk - start_map_size.y);
						else
							end_point = new Point(jj - start_map_size.x, kk - start_map_size.y);
					}
				}
			}
		}
		Def.print_arrayList(partial_map);
		
		if(end_point.x == -1){
			System.out.println("Not too entrance points found.");
			while(true);
		}
		
		//extend barriers
		//assume hallL is odd
		int extension = 1 + (hallL-1)/2;
		for(int ii = 0; ii < pm_x; ii++){
			for(int jj = 0; jj < pm_y; jj++){
				if(partial_map.get(ii).get(jj) == 1){
					int xx = 0, yy = 0;
					for(int kk = 0; kk <= extension; kk++){
						for(int qq = 0; qq <= extension; qq++){
							if((kk == 0 || qq == 0) && !(kk == 0 && qq == 0)){
								xx = ii+kk;
								yy = jj+qq;
								if(xx >= 0 && xx < pm_x && yy >= 0 && yy < pm_y){
									if(partial_map.get(xx).get(yy) == 0)
										partial_map.get(xx).set(yy, 3);
									else{
										if(kk > 0) kk = extension+1;
										if(qq > 0) qq = extension+1;
									}
								}
							}
						}
					}
					for(int kk = 0; kk >= -1*extension; kk--){
						for(int qq = 0; qq >= -1*extension; qq--){
							if((kk == 0 || qq == 0) && !(kk == 0 && qq == 0)){
								xx = ii+kk;
								yy = jj+qq;
								if(xx >= 0 && xx < pm_x && yy >= 0 && yy < pm_y){
									if(partial_map.get(xx).get(yy) == 0)
										partial_map.get(xx).set(yy, 3);
									else{
										if(kk < 0) kk = -1*(extension+1);
										if(qq < 0) qq = -1*(extension+1);
									}
								}
							}
						}
					}
				}
			}
		}
		for(int ii = 0; ii < pm_x; ii++){
			for(int jj = 0; jj < pm_y; jj++){
				if(partial_map.get(ii).get(jj) == 3)
					partial_map.get(ii).set(jj, 1);
			}
		}
		Def.print_arrayList(partial_map);
		
		ArrayList<Point> path = BFS(start_point, end_point, partial_map);
		if(path.size() == 0)
			System.out.println("No path found.");
		else{
			Def.print_arrayList(partial_map);
		}
		
	}
	
	private ArrayList<Point> BFS(Point start_point, Point end_point, ArrayList<ArrayList<Integer>> partial_map){
		int pm_x = partial_map.size(), pm_y = partial_map.get(0).size();
		ArrayList<Point> path = new ArrayList<Point>();
		
		ArrayList<Point> search_nodes = new ArrayList<Point>();
		ArrayList<Point> new_search_nodes = new ArrayList<Point>();
		HashMap<Point, Point> seen_map = new HashMap<Point, Point>();
		
		search_nodes.add(start_point);
		seen_map.put(start_point,start_point);
		boolean found = false;
		
		while(search_nodes.size() != 0 && found == false){
			for(int ii = 0; ii < search_nodes.size(); ii++){
				Point pt = search_nodes.get(ii);
				for(int jj = -1; jj <= 1; jj++){
					for(int kk = -1; kk <= 1; kk++){
						if((jj == 0 || kk == 0) && !(jj == 0 && kk == 0)){
							Point pn = new Point(pt.x + jj, pt.y + kk);
							if((pn.x >= 0) && (pn.x < pm_x) && (pn.y >= 0) && (pn.y < pm_y)){
								if(pn.x == end_point.x && pn.y == end_point.y){
									seen_map.put(end_point,pt);
									found = true;
									search_nodes.clear();
									jj = 2;
									kk = 2;
									break;
								}								
								if(partial_map.get(pn.x).get(pn.y) == 0 && !seen_map.containsKey(pn)){
									new_search_nodes.add(pn);
									seen_map.put(pn, pt);
								}
							}
						}
					}
				}
			}
			if(!found){
				search_nodes = new_search_nodes;
				new_search_nodes = new ArrayList<Point>();
			}
		}
		
		if(found){
			Point pt = seen_map.get(end_point);
			path.add(end_point);
			while(pt.x != start_point.x || pt.y != start_point.y){
				partial_map.get(pt.x).set(pt.y,4);
				path.add(pt);
				pt = seen_map.get(pt);
			}
		}
		
		return path;
	}

	private ArrayList<Point> generateEntrancePoints(int seed, ArrayList<room> rooms, int rm_1, int rm_2, ArrayList<Point> sides){
		
		//Setup
		Random rand = new Random(seed);
		room rm1 = rooms.get(rm_1), rm2 = rooms.get(rm_2);
		int hallL = Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE, grid_x = grid.size(), grid_y = grid.get(0).size();
		
		ArrayList<ArrayList<Point>> all_sides = new ArrayList<ArrayList<Point>>();
		HashMap<ArrayList<Point>, Boolean> check = new HashMap<ArrayList<Point>, Boolean>();
		ArrayList<Point> rm_points = new ArrayList<Point>();
		ArrayList<Point> entrance = new ArrayList<Point>();
		ArrayList<room> my_rooms = new ArrayList<room>();
		my_rooms.add(rm1);
		my_rooms.add(rm2);

		ArrayList<Point> output = new ArrayList<Point>();
		boolean open_found = false;
		
		
		//for each room
		for(int zz = 0; zz < my_rooms.size(); zz++){
			//I: find all possible sides
			room rm = my_rooms.get(zz);
			ArrayList<Point> first_side = new ArrayList<Point>();
			first_side.add(sides.get(zz*2));
			first_side.add(sides.get(zz*2 + 1));
			
			check.clear();
			all_sides.clear();
			rm_points.clear();
			entrance.clear();
			
			rm_points.add(rm.start);
			rm_points.add(new Point(rm.start.x,rm.end.y));
			rm_points.add(rm.end);
			rm_points.add(new Point(rm.end.x,rm.start.y));
			
			int jj = 1;
			for(int ii = 0; ii < rm_points.size(); ii++, jj++){
				if(jj == rm_points.size()) jj = 0;
				ArrayList<Point> side = new ArrayList<Point>();
				side.add(new Point(rm_points.get(ii)));
				side.add(new Point(rm_points.get(jj)));
				check.put(side, false);
				all_sides.add(side);
			}
			
			//II: Choose a side
			ArrayList<Point> cur_side = first_side;
			
			boolean side_obtained = false;
			
			
			while(!side_obtained){
				check.put(cur_side, true);
				//III: Find maximum and minimum start locations
				boolean x_const = false;
				boolean side_fail = false;
				if(cur_side.get(0).x == cur_side.get(1).x) x_const = true;
				int var_parameter_min = 0, var_parameter_max = 0, var_parameter_set = 0;
				
				if(x_const){
					var_parameter_max = Math.abs(cur_side.get(0).y-cur_side.get(1).y) - hallL - 1;	//has to be inset
					var_parameter_min = ((cur_side.get(0).y < cur_side.get(1).y) ? cur_side.get(0).y+1 : cur_side.get(1).y+1); 
				} else{
					var_parameter_max = Math.abs(cur_side.get(0).x-cur_side.get(1).x) - hallL - 1;
					var_parameter_min = ((cur_side.get(0).x < cur_side.get(1).x) ? cur_side.get(0).x+1 : cur_side.get(1).x+1); 
				}
				
				//if start location is not big enough, fail side
				if(var_parameter_max < 0) side_fail = true;
				
				if(!side_fail){
					
					//first try to randomly generate the location
					int attempts = 0;
					
					
					while(attempts++ < Def.TRY_ATTEMPTS && !side_fail && !side_obtained){
						open_found = false;
						//IV: Generate a bounded start location
						var_parameter_set = rand.nextInt(var_parameter_max + 1);
						
						//V:Create a point of that location
						Point gp;
						if(x_const){
							gp = new Point(cur_side.get(0).x, var_parameter_set + var_parameter_min);
						} else{
							gp = new Point(var_parameter_set + var_parameter_min, cur_side.get(0).y);
						}
						
						//VI: Check point for an opening by checking one square on all sides. Only one point should exist.
						Point location = new Point(-1,-1);
						for(int qq = -1; qq <= 1; qq++){
								for(int ww = -1; ww <= 1; ww++){
									if((qq == 0 || ww == 0) && !(qq == 0 && ww == 0)){
											System.out.print("(" + (gp.x+qq) + "," + (gp.y+ww) + ")");
										if((gp.x+qq >= 0) && (gp.x+qq < grid_x) && (gp.y+ww >= 0) && (gp.y+ww < grid_y))
											System.out.println(" = " + grid.get(gp.x+qq).get(gp.y+ww));
										if((gp.x+qq >= 0) && (gp.x+qq < grid_x) && (gp.y+ww >= 0) && (gp.y+ww < grid_y) && grid.get(gp.x+qq).get(gp.y+ww) == Def.UNASSIGNED_CODE){
											if(open_found){
												System.out.println("Algorithm fault. Extra opening exists at (" + gp.x+qq + "," + gp.y+ww + "). Centered at (" + gp.x + "," + gp.y + ")");
												while(true);
											} else{
												open_found = true;
												location = new Point(gp.x+qq, gp.y+ww);
											}
										}
									}
								}
						}
						
						boolean increment = false, x_direction = false;
						int var = 0;
						if(gp.x == location.x){
								var = gp.y;
								if(gp.y < location.y)
									increment = true;
						} else{
								x_direction = true;
								var = gp.x;
								if(gp.x < location.x)
									increment = true;
						 }
						
						//VII:Check if point out of bounds
						side_fail = true;
						if(increment && x_direction && var + 2 + hallL >= grid_x)
							break;
						if(increment && !x_direction && var + 2 + hallL >= grid_y)
							break;
						if(!increment && var - 2 - hallL <= 0)
							break;
						side_fail = false;							
						
						//VIII:If still good check all points
						if(open_found){
							Point start_range = new Point(0,0), end_range = new Point(0,0);
							if(x_direction && increment){
								start_range = new Point(gp.x + 1,gp.y-1);
								end_range = new Point(gp.x + 1 + hallL + 1, gp.y + hallL);
							}
							else if(x_direction && !increment){
								start_range = new Point(gp.x - 1, gp.y - 1);
								end_range = new Point(gp.x - 1 - hallL - 1, gp.y + hallL);
							}
							else if(!x_direction && increment){
								start_range = new Point(gp.x-1, gp.y+1);
								end_range = new Point(gp.x + hallL, gp.y+1+hallL+1);
							}
							else if(!x_direction && !increment){
								start_range = new Point(gp.x - 1, gp.y - 1);
								end_range = new Point(gp.x + hallL, gp.y - 1 - hallL - 1);
							}
							
							for(int qq = Def.min(start_range.x, end_range.x); qq <= Def.max(end_range.x, start_range.x); qq++){
								for(int ww = Def.min(start_range.y, end_range.y); ww <= Def.max(start_range.y, end_range.y); ww++){
									if(grid.get(qq).get(ww) != Def.UNASSIGNED_CODE)
										open_found = false;
									//update(qq,ww,Def.CURRENTLY_SENSING_AREA_CODE);
								}
							}
						 }
						//IX: If still good, make the entrance
						if(open_found){
							side_obtained = true;
							
							if(x_direction && increment){
								for(int qq = 0; qq < hallL; qq++){
									//update(gp.x, gp.y-qq, Def.CURRENTLY_SEARCHING_AREA_CODE);
									entrance.add(new Point(gp.x, gp.y-qq));
								}
							}
							else if(x_direction && !increment){
								for(int qq = 0; qq < hallL; qq++){
									//update(gp.x, gp.y+qq, Def.CURRENTLY_SEARCHING_AREA_CODE);
									entrance.add(new Point(gp.x, gp.y+qq));
								}
							}
							else if(!x_direction && increment){
								for(int qq = 0; qq < hallL; qq++){
									//update(gp.x-qq, gp.y, Def.CURRENTLY_SEARCHING_AREA_CODE);
									entrance.add(new Point(gp.x-qq, gp.y));
								}
							}
							else if(!x_direction && !increment){
								for(int qq = 0; qq < hallL; qq++){
									//update(gp.x+qq, gp.y, Def.CURRENTLY_SEARCHING_AREA_CODE);
									entrance.add(new Point(gp.x+qq, gp.y));
								}
							}
							break;
						 }
					}


					if(!side_fail && !side_obtained){
						for(int tt = 0; tt < var_parameter_max; tt++){
							
							open_found = false;
							//IV: Generate a bounded start location
							var_parameter_set = tt;
							
							//V:Create a point of that location
							Point gp;
							if(x_const){
								gp = new Point(cur_side.get(0).x, var_parameter_set + var_parameter_min);
							} else{
								gp = new Point(var_parameter_set + var_parameter_min, cur_side.get(0).y);
							}
							
							//VI: Check point for an opening by checking one square on all sides. Only one point should exist.
							Point location = new Point(-1,-1);
							for(int qq = -1; qq <= 1; qq++){
									for(int ww = -1; ww <= 1; ww++){
										if((qq == 0 || ww == 0) && !(qq == 0 && ww == 0)){
												System.out.print("(" + (gp.x+qq) + "," + (gp.y+ww) + ")");
											if((gp.x+qq >= 0) && (gp.x+qq < grid_x) && (gp.y+ww >= 0) && (gp.y+ww < grid_y))
												System.out.println(" = " + grid.get(gp.x+qq).get(gp.y+ww));
											if((gp.x+qq >= 0) && (gp.x+qq < grid_x) && (gp.y+ww >= 0) && (gp.y+ww < grid_y) && grid.get(gp.x+qq).get(gp.y+ww) == Def.UNASSIGNED_CODE){
												if(open_found){
													System.out.println("Algorithm fault. Extra opening exists at (" + gp.x+qq + "," + gp.y+ww + "). Centered at (" + gp.x + "," + gp.y + ")");
													while(true);
												} else{
													open_found = true;
													location = new Point(gp.x+qq, gp.y+ww);
												}
											}
										}
									}
							}
							
							boolean increment = false, x_direction = false;
							int var = 0;
							if(gp.x == location.x){
									var = gp.y;
									if(gp.y < location.y)
										increment = true;
							} else{
									x_direction = true;
									var = gp.x;
									if(gp.x < location.x)
										increment = true;
							 }
							
							//VII:Check if point out of bounds
							side_fail = true;
							if(increment && x_direction && var + 2 + hallL >= grid_x)
								break;
							if(increment && !x_direction && var + 2 + hallL >= grid_y)
								break;
							if(!increment && var - 2 - hallL <= 0)
								break;
							side_fail = false;							
							
							//VIII:If still good check all points
							if(open_found){
								Point start_range = new Point(0,0), end_range = new Point(0,0);
								if(x_direction && increment){
									start_range = new Point(gp.x + 1,gp.y-1);
									end_range = new Point(gp.x + 1 + hallL + 1, gp.y + hallL);
								}
								else if(x_direction && !increment){
									start_range = new Point(gp.x - 1, gp.y - 1);
									end_range = new Point(gp.x - 1 - hallL - 1, gp.y + hallL);
								}
								else if(!x_direction && increment){
									start_range = new Point(gp.x-1, gp.y+1);
									end_range = new Point(gp.x + hallL, gp.y+1+hallL+1);
								}
								else if(!x_direction && !increment){
									start_range = new Point(gp.x - 1, gp.y - 1);
									end_range = new Point(gp.x + hallL, gp.y - 1 - hallL - 1);
								}
								
								for(int qq = Def.min(start_range.x, end_range.x); qq <= Def.max(end_range.x, start_range.x); qq++){
									for(int ww = Def.min(start_range.y, end_range.y); ww <= Def.max(start_range.y, end_range.y); ww++){
										if(grid.get(qq).get(ww) != Def.UNASSIGNED_CODE)
											open_found = false;
										//update(qq,ww,Def.CURRENTLY_SENSING_AREA_CODE);
									}
								}
							 }
							//IX: If still good, make the entrance
							if(open_found){
								side_obtained = true;
								
								if(x_direction && increment){
									for(int qq = 0; qq < hallL; qq++){
										//update(gp.x, gp.y-qq, Def.CURRENTLY_SEARCHING_AREA_CODE);
										entrance.add(new Point(gp.x, gp.y-qq));
									}
								}
								else if(x_direction && !increment){
									for(int qq = 0; qq < hallL; qq++){
										//update(gp.x, gp.y+qq, Def.CURRENTLY_SEARCHING_AREA_CODE);
										entrance.add(new Point(gp.x, gp.y+qq));
									}
								}
								else if(!x_direction && increment){
									for(int qq = 0; qq < hallL; qq++){
										//update(gp.x-qq, gp.y, Def.CURRENTLY_SEARCHING_AREA_CODE);
										entrance.add(new Point(gp.x-qq, gp.y));
									}
								}
								else if(!x_direction && !increment){
									for(int qq = 0; qq < hallL; qq++){
										//update(gp.x+qq, gp.y, Def.CURRENTLY_SEARCHING_AREA_CODE);
										entrance.add(new Point(gp.x+qq, gp.y));
									}
								}
								break;
							 }
						}
					}
				}
				if(!side_obtained){
					boolean new_side_found = false;
					for(int qq = 0; qq < all_sides.size(); qq++){
						if(!check.get(all_sides.get(qq))){
							new_side_found = true;
							cur_side = all_sides.get(qq);
							break;
						}
					}
					//room doesn't work
					if(!new_side_found){
						output.clear();
						output.add(new Point(zz,-1));
						return output;
					}
				} else{
					output.add(entrance.get(0));
					output.add(entrance.get(entrance.size()-1));
				}
			}
		}
			
		return output;
	}

	
	private int find_closest_room(ArrayList<room> rooms, int cur_room, HashMap<Integer,Boolean> exclusions){
		Point x1y1 = rooms.get(cur_room).start, x1y2 = new Point(rooms.get(cur_room).start.x, rooms.get(cur_room).end.y), x2y1 = new Point(rooms.get(cur_room).end.x, rooms.get(cur_room).start.y), x2y2 = rooms.get(cur_room).end; 
		ArrayList<Point> cur_points = new ArrayList<Point>();
		cur_points.add(x1y1);
		cur_points.add(x1y2);
		cur_points.add(x2y1);
		cur_points.add(x2y2);
		int closest_room = -1;
		double distance = -1;
		boolean found = false;
		for(int ii = 0; ii < rooms.size(); ii++){
			if(ii != cur_room && exclusions.containsKey(ii) == false){
				found = true;
				room rm = rooms.get(ii);
				Point n_x1y1 = rm.start, n_x2y2 = rm.end;
				Point n_x2y1 = new Point(n_x2y2.x, n_x1y1.y), n_x1y2 = new Point(n_x1y1.x, n_x2y2.y);
				ArrayList<Point> new_points = new ArrayList<Point>();
				new_points.add(n_x1y1);
				new_points.add(n_x2y2);
				new_points.add(n_x2y1);
				new_points.add(n_x1y2);
				for(int jj = 0; jj < cur_points.size(); jj++){
					for(int kk = 0; kk < new_points.size(); kk++){
						double new_distance = Def.get_distance(cur_points.get(jj), new_points.get(jj));
						if(distance == -1 || distance > new_distance){
							closest_room = ii;
							distance = new_distance;
						}
					}
				}
			}
		}
		if(!found)
			return -1;
		return closest_room;
	}
	
	private ArrayList<Point> find_opposing_faces(ArrayList<room> rooms, int rm_1, int rm_2){
		room rm1 = rooms.get(rm_1), rm2 = rooms.get(rm_2);
		Point x1y1_1 = rm1.start, x2y2_1 = rm1.end;
		Point x1y2_1 = new Point(x1y1_1.x, x2y2_1.y), x2y1_1 = new Point(x2y2_1.x, x1y1_1.y);
		Point x1y1_2 = rm2.start, x2y2_2 = rm2.end;
		Point x1y2_2 = new Point(x1y1_2.x, x2y2_2.y), x2y1_2 = new Point(x2y2_2.x, x1y1_2.y);
	
		ArrayList<Point> centerpoints_1 = new ArrayList<Point>();
		ArrayList<Point> coords_1 = new ArrayList<Point>();
		centerpoints_1.add(new Point((x1y1_1.x+x2y1_1.x)/2,(x1y1_1.y+x2y1_1.y)/2));
		coords_1.add(x1y1_1);
		coords_1.add(x2y1_1);
		centerpoints_1.add(new Point((x2y1_1.x+x2y2_1.x)/2,(x2y1_1.y+x2y2_1.y)/2));
		coords_1.add(x2y1_1);
		coords_1.add(x2y2_1);
		centerpoints_1.add(new Point((x2y2_1.x+x1y2_1.x)/2,(x2y2_1.y+x1y2_1.y)/2));
		coords_1.add(x2y2_1);
		coords_1.add(x1y2_1);
		centerpoints_1.add(new Point((x1y2_1.x+x1y1_1.x)/2,(x1y2_1.y+x1y1_1.y)/2));
		coords_1.add(x1y2_1);
		coords_1.add(x1y1_1);
		
		ArrayList<Point> centerpoints_2 = new ArrayList<Point>();
		ArrayList<Point> coords_2 = new ArrayList<Point>();
		centerpoints_2.add(new Point((x1y1_2.x+x2y1_2.x)/2,(x1y1_2.y+x2y1_2.y)/2));
		coords_2.add(x1y1_2);
		coords_2.add(x2y1_2);
		centerpoints_2.add(new Point((x2y1_2.x+x2y2_2.x)/2,(x2y1_2.y+x2y2_2.y)/2));
		coords_2.add(x2y1_2);
		coords_2.add(x2y2_2);
		centerpoints_2.add(new Point((x2y2_2.x+x1y2_2.x)/2,(x2y2_2.y+x1y2_2.y)/2));
		coords_2.add(x2y2_2);
		coords_2.add(x1y2_2);
		centerpoints_2.add(new Point((x1y2_2.x+x1y1_2.x)/2,(x1y2_2.y+x1y1_2.y)/2));
		coords_2.add(x1y2_2);
		coords_2.add(x1y1_2);
		
		int dir_1 = 0, dir_2 = 0;
		double distance = -1;
		for(int ii = 0; ii < centerpoints_1.size(); ii++){
			for(int jj = 0; jj < centerpoints_2.size(); jj++){
				double cur_distance = Def.get_distance(centerpoints_1.get(ii), centerpoints_2.get(jj));
				if(distance == -1 || distance > cur_distance){
					distance = cur_distance;
					dir_1 = ii;
					dir_2 = jj;
				}
			}
		}
		
		ArrayList<Point> output = new ArrayList<Point>();
		output.add(coords_1.get(dir_1*2));
		output.add(coords_1.get((dir_1*2) + 1));
		output.add(coords_2.get(dir_2*2));
		output.add(coords_2.get((dir_2*2) + 1));
		
		return output;
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
	
	public void color(Point start, Point end, int type){
		for(int ii = start.x; ii <= end.x; ii++){
			for(int jj = start.y; jj <= end.y; jj++){
				update(ii,jj,type);
			}
		}
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
	/*	else if(type < 1 || type > 9)
			return -1;
	*/
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
	public int ID;
	public int doors;
	public Point start;
	public Point end;
	public boolean accessed;
	public ArrayList<Integer> connected_to;
	public Map mm;
	
	public room(int ID, int doors, Point start, Point end, Map mm){
		this.ID = ID;
		this.doors = doors;
		this.start = start;
		this.end = end;
		this.accessed = false;
		this.connected_to = new ArrayList<Integer>();
		this.mm = mm;
	}
	
	public void color(int color_code){
		for(int ii = start.x; ii <= end.x; ii++){
			for(int jj = start.y; jj <= end.y; jj++){
				System.out.println("(" + ii + "," + jj + ")");
				mm.update(ii, jj, color_code);
			}
		}
	}
}
