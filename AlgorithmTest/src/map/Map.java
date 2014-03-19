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
import gui.full_grid;

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
 */
public class Map extends ProtoMap{
	
	//maintains a numeric code 2d array
	private ArrayList<ArrayList<Integer>> grid;
	private BufferedWriter log;		//logging 
	private full_grid gui;	//used to call update functions
	
	private Point quadcopter_position = new Point(-1, -1);
	private double quadcopter_direction;
	
	
	/**
	 * Map constructor
	 * Initializes default values. Checks and fits default values so that they work best together
	 * @param log_file 
	 * @throws IOException: If there is an issue with logging file
	 */
	@SuppressWarnings("unused")
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
		resize(sq_row, sq_col, 0);
	}
	
	public void setQuadcopterPositionAndDirection(Point position, double direction){
		quadcopter_direction = direction;
		update(position.x, position.y, Def.QUADCOPTER_CODE);
	}
	
	public Point getQuadcopterPosition(){return quadcopter_position;}
	public double getQuadcopterDirection(){return quadcopter_direction;}
	public Point getSizeOfGrid(){ return new Point(grid.size(), grid.get(0).size()); }
	
	public void resize(int sq_row, int sq_col, int val){
		for(int ii = 0; ii < sq_row; ii++){
			grid.add(new ArrayList<Integer>());
			for(int jj = 0; jj < sq_col; jj++){
				grid.get(ii).add(val);
			}
		}
	}
	
	/**
	 * Loads a premade map. Only certain maps are accepted
	 * @param map - can be Def.SAMPLE_MAP_1
	 * @throws IOException - if the file cannot be opened
	 */
	public void load_map(String map) throws IOException{
		if(map.equals(Def.SAMPLE_MAP_1) || map.equals(Def.SAMPLE_MAP_2)){
			Def.output(log, "Loading map sample map...\r\n");
			
			BufferedReader br = new BufferedReader(new FileReader(map));
			String line = br.readLine();
			ArrayList<String> lines = new ArrayList<String>();
			while(line != null){
				lines.add(line);
				line = br.readLine();
			}
			br.close();
			
			resize(lines.size(), lines.get(0).length(), 0);
			for(int ii = 0; ii < lines.size(); ii++){
				for(int jj = 0; jj < lines.get(ii).length(); jj++){
					update(ii, jj, (int)(lines.get(ii).charAt(jj) - '0'));
				}
			}
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
		room start_position_room = new room(0, 1, start_pos_start, start_pos_end, this, Def.NUMBER_OF_ROOMS);
		start_position_room.generateFaces();
		
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
			room new_room = new room(ii+1, 0, start, end, this, Def.NUMBER_OF_ROOMS);
			new_room.generateFaces();
			rooms.add(new_room);
			
			Def.output(log, String.format("Creating room between (%d,%d) and (%d,%d)\r\n", start.x, start.y, end.x, end.y));
			
		}
		
		
		/****** generate Hallways *******/
		
		
		
		for(int ee = 0; ee < rooms.size(); ee++)
			rooms.get(ee).connections = Def.ROOM_CONNECTIONS_MIN + rand.nextInt((Def.ROOM_CONNECTIONS_MAX-Def.ROOM_CONNECTIONS_MIN)+1);
		
		boolean fully_connected = false;
		
		while(!fully_connected){
			fully_connected = true;
			for(int zz = 0; zz < rooms.size(); zz++){
				room rm = rooms.get(zz);
				if(rm.connected_to.size() < rm.connections){
					fully_connected = false;
					boolean unconnected = true;
					while(unconnected){
						int rm2 = find_closest_room(rooms, zz);
						if(rm2 == -1){
							System.out.println("Cannot connect room " + zz);
							rm.connected_to.add(-1);
							unconnected = false;
						} else{
							boolean face_chosen = false;
							while(!face_chosen){
								ArrayList<DoublePoint> faces = find_closest_face(rooms, zz, rm2);
								if(faces == null){
									rm.excludeRoom(rm2);
									face_chosen = true;
								}else{
									face_chosen = GenerateEntrancePointsAndPath(rand, rooms, zz, rm2, faces);
									if(!face_chosen){
										rm.excludeOpposingFace(rm2, faces.get(0), faces.get(1));
									} else{
										unconnected = false;
										rm.connected_to.add(rm2);
										rooms.get(rm2).connected_to.add(zz);
										rm.excludeRoom(rm2);
									}
								}
							}
						}
					}
				}
			}
		}
		//add borders to rooms
		for(int ii = 0; ii < rooms.size(); ii++){
			rooms.get(ii).addBorder();
		}
		
		//fill in blanks
		for(int ii = 0; ii < grid_x; ii++){
			for(int jj = 0; jj < grid_y; jj++){
				if(grid.get(ii).get(jj) == 0)
					update(ii, jj, Def.WALL_CODE);
			}
		}
		
		//add survivors
		int survivors = 0;
		while(survivors < Def.NUMBER_OF_SURVIVORS){
			if(survivors == Def.NUMBER_OF_SURVIVORS)
				break;
			for(int ii = 0; ii < grid_x; ii++){
				if(survivors == Def.NUMBER_OF_SURVIVORS)
					break;
				for(int jj = 0; jj < grid_y; jj++){
					if(survivors == Def.NUMBER_OF_SURVIVORS)
						break;
					else{
						if(grid.get(ii).get(jj) == Def.MOVABLE_AREA_CODE){
							if(rand.nextInt(1000) == 1){
								update(ii, jj, Def.PERSON_CODE);
								survivors++;
							}
						}
					}
				}
			}
		}
	}
	
	private ArrayList<DoublePoint> find_closest_face(ArrayList<room> rooms, int rm_1, int rm_2){
		//generate list of distances
		ArrayList<DoublePoint> rm1_sides = rooms.get(rm_1).faces;
		ArrayList<DoublePoint> rm2_sides = rooms.get(rm_2).faces;
		
		ArrayList<ArrayList<DoublePoint>> available_sides = new ArrayList<ArrayList<DoublePoint>>();
		for(int ii = 0; ii < rm1_sides.size(); ii++){
			if(!rooms.get(rm_1).isFaceExcluded(rm_2, rm1_sides.get(ii))){
				for(int jj = 0; jj < rm2_sides.size(); jj++){
					if(!rooms.get(rm_1).isOpposingFaceExcluded(rm_2, rm1_sides.get(ii), rm2_sides.get(jj))){
						ArrayList<DoublePoint> pair = new ArrayList<DoublePoint>();
						pair.add(rm1_sides.get(ii));
						pair.add(rm2_sides.get(jj));
						available_sides.add(pair);
					}
				}
			}
		}
		
		if(available_sides.size() == 0)
			return null;
		else{
			int index = -1;
			double min = -1;
			for(int ii = 0; ii < available_sides.size(); ii++){
				double distance = available_sides.get(ii).get(0).getDistance(available_sides.get(ii).get(1));
				if(min == -1 || distance < min){
					min = distance;
					index = ii;
				}
			}
			
			return available_sides.get(index);
		}
	}
	
	private boolean pathing(Random rand, ArrayList<room> rooms, int rm_1, int rm_2, ArrayList<Point> entrances, Point start_map_size, Point end_map_size){
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
		ArrayList<ArrayList<Integer>> partial_map_save = new ArrayList<ArrayList<Integer>>();
		for(int ii = 0; ii < pm_x; ii++){
			ArrayList<Integer> row = new ArrayList<Integer>();
			ArrayList<Integer> row2 = new ArrayList<Integer>();
			for(int jj = 0; jj < pm_y; jj++){
				row.add(0);
				row2.add(0);
			}
			partial_map.add(row);
			partial_map_save.add(row2);
		}
		//System.out.println("Creating partial map based on points " + Def.print_point(start_map_size) + " and " + Def.print_point(end_map_size) + " with dimensions " + pm_x + "x" + pm_y);
		
		//populate partial map
		//System.out.println(" ");
		
		for(int ii = 0; ii < pm_x; ii++){
			for(int jj = 0; jj < pm_y; jj++){
				try{
					if(grid.get(start_map_size.x+ii).get(start_map_size.y+jj) != 0)
						partial_map.get(ii).set(jj, 1);
					else
						partial_map.get(ii).set(jj, 0);
				}catch(Exception e){
					System.out.println("Error");
				}
					//System.out.print(" " + partial_map.get(ii).get(jj));
			}
			//System.out.println(" ");
		}
		
		//save map
		for(int ii = 0; ii < pm_x; ii++){
			for(int jj = 0; jj < pm_y; jj++){
				partial_map_save.get(ii).set(jj, partial_map.get(ii).get(jj));
			}
		}
		
		//add entrance point
		Point e_s, e_e;
		Point start_point = new Point(-1,-1), end_point = new Point(-1,-1);
		for(int ii = 0; ii < 2; ii++){
			e_s = new Point(Def.min(entrances.get(ii*2).x, entrances.get(ii*2+1).x), Def.min(entrances.get(ii*2).y, entrances.get(ii*2+1).y));
			e_e = new Point(Def.max(entrances.get(ii*2).x, entrances.get(ii*2+1).x), Def.max(entrances.get(ii*2).y, entrances.get(ii*2+1).y));
			//System.out.println(Def.print_point(e_s) + " " + Def.print_point(e_e));
			for(int jj = e_s.x; jj <= e_e.x; jj++){
				for(int kk = e_s.y; kk <= e_e.y; kk++){
					try{
						partial_map.get(jj-start_map_size.x).set(kk-start_map_size.y, 1);
						if(jj == (int)Math.ceil((e_s.x + e_e.x)/2) && kk == (int)Math.ceil((e_s.y+e_e.y)/2)){
							partial_map.get(jj-start_map_size.x).set(kk-start_map_size.y, 2);
							if(start_point.x == -1)
								start_point = new Point(jj - start_map_size.x, kk - start_map_size.y);
							else
								end_point = new Point(jj - start_map_size.x, kk - start_map_size.y);
						}
					} catch(Exception e){
						System.out.println("Error");
					}
				}
			}
		}
		//Def.print_arrayList(partial_map);
		
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
		//Def.print_arrayList(partial_map);
		
		ArrayList<Point> path = BFS(start_point, end_point, partial_map);
		if(path.size() == 0){
			System.out.println("No path found.");
			Point start_new = new Point(start_map_size.x-5, start_map_size.y-5);
			if(start_new.x < 0) start_new.x = 0;
			if(start_new.y < 0) start_new.y = 0;
			Point end_new = new Point(end_map_size.x+5, end_map_size.y+5);
			if(end_new.x >= grid_x) end_new.x = grid_x-1;
			if(end_new.y >= grid_y) end_new.y = grid_y-1;
			if(start_new.x == start_map_size.x && start_new.y == start_map_size.y && end_new.x == end_map_size.x && end_new.y == end_map_size.y)
				return false;
			return pathing(rand, rooms, rm_1, rm_2, entrances, start_new, end_new);
		}			
		else{
			//Def.print_arrayList(partial_map);
			//Def.print_arrayList(partial_map_save);
			
			//update mini-map
			for(int ii = 0; ii < path.size(); ii++){
				partial_map_save.get(path.get(ii).x).set(path.get(ii).y,2);
			}
			
			//Def.print_arrayList(partial_map_save);
			for(int ii = 0; ii < pm_x; ii++){
				for(int jj = 0; jj < pm_y; jj++){
					if(partial_map_save.get(ii).get(jj) == 2){
						for(int kk = 0; kk >= -1*(hallL/2); kk--){
							for(int qq = 0; qq >= -1*(hallL/2); qq--){
								if((kk == 0 || qq == 0) && !(kk == 0 && qq == 0)){
									int xx = ii+kk;
									int yy = jj+qq;
									if(xx >= 0 && xx < pm_x && yy >= 0 && yy < pm_y){
										if(partial_map_save.get(xx).get(yy) != 0){
											if(kk < 0) kk = -1*hallL;
											if(qq < 0) qq = -1*hallL;
										}
										else
											partial_map_save.get(xx).set(yy, 3);
									}
								}
							}
						}
						for(int kk =0; kk <= (hallL/2); kk++){
							for(int qq = 0; qq <= (hallL/2); qq++){
								if((kk == 0 || qq == 0) && !(kk == 0 && qq == 0)){
									int xx = ii+kk;
									int yy = jj+qq;
									if(xx >= 0 && xx < pm_x && yy >= 0 && yy < pm_y){
										if(partial_map_save.get(xx).get(yy) != 0){
											if(kk > 0) kk = hallL;
											if(qq > 0) qq = hallL;
										}
										else
											partial_map_save.get(xx).set(yy, 3);
									}
								}
							}
						}
					}
				}
			}
			
			for(int ii = 0; ii < pm_x; ii++){
				for(int jj = 0; jj < pm_y; jj++){
					if(partial_map_save.get(ii).get(jj) == 3)
						partial_map_save.get(ii).set(jj, 2);
				}
			}
			//Def.print_arrayList(partial_map_save);
			
			//update main map
			for(int ii = 0; ii < pm_x; ii++){
				for(int jj = 0; jj < pm_y; jj++){
					if(partial_map_save.get(ii).get(jj) == 2){
						int xx = start_map_size.x + ii, yy= start_map_size.y + jj;
						if(grid.get(xx).get(yy) == Def.UNASSIGNED_CODE)
							update(xx, yy, Def.MOVABLE_AREA_CODE);
					}						
				}
			}
			
		}
		return true;
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

	private boolean GenerateEntrancePointsAndPath(Random rand, ArrayList<room> rooms, int rm_1, int rm_2, ArrayList<DoublePoint> faces){
		
		//Setup
		
		
		int hallL = Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE;
		ArrayList<DoublePoint> rm1_exclusions = new ArrayList<DoublePoint>();
		ArrayList<DoublePoint> rm1_entrance_reference = new ArrayList<DoublePoint>();
		ArrayList<ArrayList<DoublePoint>> rm2_exclusions = new ArrayList<ArrayList<DoublePoint>>();
		ArrayList<DoublePoint> entrances = new ArrayList<DoublePoint>();
		
		int zz = 0;
		boolean pass = false;
		
		//for each room
		while(!pass){
			//I: find all possible sides
	
			DoublePoint side = faces.get(zz);				
				
			//III: Find maximum and minimum start locations
			boolean x_const = false;
			DoublePoint entrance = null;
			
			if(side.p1.x == side.p2.x) x_const = true;
			int var_parameter_min = 0, var_parameter_max = 0, var_parameter_set = 0;
			
			if(x_const){
				var_parameter_max = Math.abs(side.p1.y-side.p2.y) - hallL - 1;	//has to be inset
				var_parameter_min = ((side.p1.y < side.p2.y) ? side.p1.y+1 : side.p2.y+1); 
			} else{
				var_parameter_max = Math.abs(side.p1.x-side.p2.x) - hallL - 1;
				var_parameter_min = ((side.p1.x < side.p2.x) ? side.p1.x+1 : side.p2.x+1); 
			}
				
			//if start location is not big enough, fail side
			if(var_parameter_max < 0) return false;
						
			//first try to randomly generate the location
			int attempts = 0;		
			
			boolean sideObtained = false;
			
			while(!sideObtained && attempts++ < Def.TRY_ATTEMPTS){
				DoublePoint pp = null;
				if(zz == 0)
					pp = createEntrance(rand, var_parameter_max, var_parameter_min, var_parameter_set, x_const, faces.get(zz), rm1_exclusions, false);
				else{
					int index = -1;
					for(int ii = 0; ii < rm1_entrance_reference.size(); ii++){
						if(rm1_entrance_reference.get(ii).equals(entrances.get(0))){
							index = ii;
							break;
						}
					}
					
					if(index == -1){
						rm1_entrance_reference.add(entrances.get(0));
						rm2_exclusions.add(new ArrayList<DoublePoint>());
						index = rm1_entrance_reference.size()-1;
					}
					
					pp = createEntrance(rand, var_parameter_max, var_parameter_min, var_parameter_set, x_const, faces.get(zz), rm2_exclusions.get(index), false);
				}
				
				
				//check if excluded
				if(pp != null){
					if(zz == 0){
						for(int ii = 0; ii < rm1_exclusions.size(); ii++){
							if(rm1_exclusions.get(ii).equals(pp)){
								pp = null;
								break;
							}							
						}
					}else{
						int index = -1;
						for(int ii = 0; ii < rm1_entrance_reference.size(); ii++){
							if(rm1_entrance_reference.get(ii).equals(entrances.get(0))){
								index = ii;
								break;
							}
						}
						
						for(int ii = 0; ii < rm2_exclusions.get(index).size(); ii++){
							if(rm2_exclusions.get(index).get(ii).equals(pp)){
								pp = null;
								break;
							}
						}
					}
				}
				
				//IX: If still good, make the entrance
				if(pp != null){					
					entrance = pp;
					entrances.add(entrance);
					sideObtained = true;
				}
			}
			if(!sideObtained){
				for(int tt = 0; tt < var_parameter_max; tt++){
					DoublePoint pp = null;
					if(zz == 0)
						pp = createEntrance(rand, var_parameter_max, var_parameter_min, tt, x_const, faces.get(zz), rm1_exclusions, true);
					else{
						int index = -1;
						for(int ii = 0; ii < rm1_entrance_reference.size(); ii++){
							if(rm1_entrance_reference.get(ii).equals(entrances.get(0))){
								index = ii;
								break;
							}
						}
						
						if(index == -1){
							rm1_entrance_reference.add(entrances.get(0));
							rm2_exclusions.add(new ArrayList<DoublePoint>());
							index = rm1_entrance_reference.size()-1;
						}
						
						pp = createEntrance(rand, var_parameter_max, var_parameter_min, var_parameter_set, x_const, faces.get(zz), rm2_exclusions.get(index), true);
					}
					
					
					//check if excluded
					if(pp != null){
						if(zz == 0){
							for(int ii = 0; ii < rm1_exclusions.size(); ii++){
								if(rm1_exclusions.get(ii).equals(pp)){
									pp = null;
									break;
								}							
							}
						}else{
							int index = -1;
							for(int ii = 0; ii < rm1_entrance_reference.size(); ii++){
								if(rm1_entrance_reference.get(ii).equals(entrances.get(0))){
									index = ii;
									break;
								}
							}
							
							for(int ii = 0; ii < rm2_exclusions.get(index).size(); ii++){
								if(rm2_exclusions.get(index).get(ii).equals(pp)){
									pp = null;
									break;
								}
							}
						}
					}
					//IX: If still good, make the entrance
					if(pp != null){					
						entrance = pp;
						entrances.add(entrance);
						sideObtained = true;
						break;
					}	
				}
			}
			if(!sideObtained && zz == 0) return false;
			else if(!sideObtained && zz == 1){ 
				zz--;
				rm1_exclusions.add(entrances.get(0));
				entrances.clear();
			}
			else{
				if(zz == 0) zz++;
				else{
					ArrayList<Point> pts = new ArrayList<Point>();
					pts.add(entrances.get(0).p1);
					pts.add(entrances.get(0).p2);
					pts.add(entrances.get(1).p1);
					pts.add(entrances.get(1).p2);
					pass = pathing(rand, rooms, rm_1, rm_2, pts, new Point(-1,-1), new Point(-1,-1));
					if(!pass){
						int index = -1;
						for(int ii = 0; ii < rm1_entrance_reference.size(); ii++){
							if(rm1_entrance_reference.get(ii).equals(entrances.get(0))){
								index = ii;
								break;
							}
						}
						rm2_exclusions.get(index).add(entrances.get(1));		
						entrances.remove(1);
					} else{
						rooms.get(rm_1).entrance_points.add(entrances.get(0));
						rooms.get(rm_2).entrance_points.add(entrances.get(1));
					}
				}		
			}
		}
		return true;
	}
	
	public DoublePoint createEntrance(Random rand, int var_parameter_max, int var_parameter_min, int var_parameter_set, boolean x_const, DoublePoint side, ArrayList<DoublePoint> exclusions, boolean set){
		int hallL = Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE, grid_x = grid.size(), grid_y = grid.get(0).size();
		
		if(!set) var_parameter_set = rand.nextInt(var_parameter_max + 1);
		
		Point gp;
		if(x_const){
			gp = new Point(side.p1.x, var_parameter_set + var_parameter_min);
		} else{
			gp = new Point(var_parameter_set + var_parameter_min, side.p1.y);
		}
		
		boolean open_found = false;
		Point location = new Point(-1,-1);
		for(int qq = -1; qq <= 1; qq++){
				for(int ww = -1; ww <= 1; ww++){
					if((qq == 0 || ww == 0) && !(qq == 0 && ww == 0)){
						//	System.out.print("(" + (gp.x+qq) + "," + (gp.y+ww) + ")");
						if((gp.x+qq >= 0) && (gp.x+qq < grid_x) && (gp.y+ww >= 0) && (gp.y+ww < grid_y))
						//	System.out.println(" = " + grid.get(gp.x+qq).get(gp.y+ww));
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
		
		DoublePoint entr = new DoublePoint(new Point(-1,-1), new Point(-1, -1));
		if(x_direction && increment) entr = new DoublePoint(new Point(gp.x, gp.y), new Point(gp.x, gp.y - (hallL-1)));
		else if(x_direction && !increment) entr = new DoublePoint(new Point(gp.x, gp.y), new Point(gp.x, gp.y + (hallL-1)));
		else if(!x_direction && increment) entr = new DoublePoint(new Point(gp.x - (hallL-1), gp.y), new Point(gp.x, gp.y));
		else if(!x_direction && !increment) entr = new DoublePoint(new Point(gp.x + (hallL-1), gp.y), new Point(gp.x, gp.y));
		
		
		
		if(checkEntrance(gp, increment, x_direction, var))		
			return entr;
		else{
			exclusions.add(entr);
			return null;
		}
	}
	
	public boolean checkEntrance(Point gp, boolean increment, boolean x_direction, int var){
		
		int hallL = Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE, grid_x = grid.size(), grid_y = grid.get(0).size();
		
		//VI: Check point for an opening by checking one square on all sides. Only one point should exist.
		boolean open_found = true;
		//VII:Check if point out of bounds
		if(increment && x_direction && var + 2 + hallL >= grid_x)
			return false;
		if(increment && !x_direction && var + 2 + hallL >= grid_y)
			return false;
		if(!increment && var - 2 - hallL <= 0)
			return false;						
		
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
					try{
						if(qq < 0 || qq >= grid_x || ww < 0 || ww >= grid_y)
							return false;
						else if(grid.get(qq).get(ww) != Def.UNASSIGNED_CODE)
							return false;
						//update(qq,ww,Def.CURRENTLY_SENSING_AREA_CODE);
					} catch(Exception ee){
						System.out.println("Exception");
					}
				}
			}
		 }
		return open_found;
	}

	private int find_closest_room(ArrayList<room> rooms, int cur_room){
		Point x1y1 = rooms.get(cur_room).start, x1y2 = new Point(rooms.get(cur_room).start.x, rooms.get(cur_room).end.y), x2y1 = new Point(rooms.get(cur_room).end.x, rooms.get(cur_room).start.y), x2y2 = rooms.get(cur_room).end; 
		ArrayList<Point> cur_points = new ArrayList<Point>();
		cur_points.add(x1y1);
		cur_points.add(x1y2);
		cur_points.add(x2y1);
		cur_points.add(x2y2);
		int closest_room = -1;
		double distance = -1;
		boolean found = false;
		ArrayList<Integer> exclusions = rooms.get(cur_room).room_exclusions;
		for(int ii = 0; ii < rooms.size(); ii++){
			if(ii != cur_room && exclusions.contains(ii) == false){
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
	
	/**
	 * gets the value at the specified xx, yy values
	 * @param xx 
	 * @param yy
	 * @return -1 if xx,yy are invalid or the value in the grid
	 */
	@Override
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
			if(type == Def.QUADCOPTER_CODE){
				if(quadcopter_position.x != -1)
					update(quadcopter_position.x, quadcopter_position.y, Def.SENSED_AND_SEARCHED_AREA_CODE);
				quadcopter_position = new Point(xx, yy);
			}
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
	 * Prints a ascii version of the map to map.txt
	 * @throws IOException - if there is an issue with map.txt
	 */
	public void printMap(String file) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
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
	public void attachGUI(full_grid gui){ this.gui = gui;}
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
	public ArrayList<Integer> room_exclusions = new ArrayList<Integer>();
	public ArrayList<DoublePoint> faces = new ArrayList<DoublePoint>();
	public ArrayList<ArrayList<DoublePoint>> face_exclusions = new ArrayList<ArrayList<DoublePoint>>();
	public ArrayList<ArrayList<ArrayList<DoublePoint>>> opposing_face_exclusions = new ArrayList<ArrayList<ArrayList<DoublePoint>>>();
	
	public Map mm;
	public int connections;
	public ArrayList<DoublePoint> entrance_points = new ArrayList<DoublePoint>();
	
	public room(int ID, int doors, Point start, Point end, Map mm, int rooms){
		this.ID = ID;
		this.doors = doors;
		this.start = start;
		this.end = end;
		this.accessed = false;
		this.connected_to = new ArrayList<Integer>();
		this.mm = mm;
		this.connections = 0;
		for(int ii = 0; ii < rooms; ii++){
			face_exclusions.add(new ArrayList<DoublePoint>());
			ArrayList<ArrayList<DoublePoint>> fff = new ArrayList<ArrayList<DoublePoint>>();
			for(int jj = 0; jj < 4; jj++){
				ArrayList<DoublePoint> qqq = new ArrayList<DoublePoint>();
				fff.add(qqq);
			}
			opposing_face_exclusions.add(fff);
		}
	}

	public void generateFaces(){
		Point x1y1, x1y2, x2y1, x2y2;
		x1y1 = start;
		x1y2 = new Point(start.x, end.y);
		x2y1 = new Point(end.x, start.y);
		x2y2 = end;
		faces.clear();
		faces.add(new DoublePoint(x1y1, x1y2));
		faces.add(new DoublePoint(x1y2, x2y2));
		faces.add(new DoublePoint(x2y2, x2y1));
		faces.add(new DoublePoint(x2y1, x1y1));
	}
	
	public void addBorder(){
		ArrayList<Point> fillPoints = new ArrayList<Point>();
		ArrayList<Point> entrancePoints = new ArrayList<Point>();
		
		for(int ii = 0; ii < entrance_points.size(); ii++){
			DoublePoint p = entrance_points.get(ii);
			for(int jj = p.p1.x; jj <= p.p2.x; jj++){
				for(int kk = p.p1.y; kk <= p.p2.y; kk++)
					entrancePoints.add(new Point(jj, kk));
			}
		}
		
		for(int ii = 0; ii < faces.size(); ii++){
			DoublePoint p = faces.get(ii);
			for(int jj = p.p1.x; jj <= p.p2.x; jj++){
				for(int kk = p.p1.y; kk <= p.p2.y; kk++){
					if(!entrancePoints.contains(new Point(jj, kk)))
						fillPoints.add(new Point(jj, kk));
				}
			}
		}
		
		for(int ii = 0; ii < fillPoints.size(); ii++)
			mm.update(fillPoints.get(ii).x, fillPoints.get(ii).y, Def.WALL_CODE);
	}
	
	public boolean isRoomExcluded(int room){
		if(room >= 0 && room < room_exclusions.size() && room != ID){
			for(int ii = 0; ii < room_exclusions.size(); ii++){
				if(room_exclusions.get(ii) == room)
					return true;
			}
			return false;
		}
		return true;
	}
	
	public boolean isFaceExcluded(int room, DoublePoint face){
		if(room >= 0 && room < face_exclusions.size() && room != ID){
			ArrayList<DoublePoint> fff = face_exclusions.get(room);
			for(int ii = 0; ii < fff.size(); ii++){
				if(fff.get(ii).equals(face))
					return true;
			}
			return false;
		}
		return true;
	}
	
	public boolean isOpposingFaceExcluded(int room, DoublePoint face, DoublePoint oface){
		if(room >= 0 && room < opposing_face_exclusions.size() && room != ID){
			ArrayList<ArrayList<DoublePoint>> fff = opposing_face_exclusions.get(room);
			for(int ii = 0; ii < faces.size(); ii++){
				if(faces.get(ii).equals(face)){
					ArrayList<DoublePoint> qqq = fff.get(ii);
					for(int jj = 0; jj < qqq.size(); jj++){
						if(qqq.get(jj).equals(oface))
							return true;
					}
					return false;
				}
			}
			return true;
		}
		return true;
	}
	
	public ArrayList<DoublePoint> getFaceExclusions(int room){
		if(room >= 0 && room < face_exclusions.size())
			return face_exclusions.get(room);
		return null;
	}
	
	public ArrayList<DoublePoint> getOpposingFaceExclusions(int room, DoublePoint face){
		if(room >= 0 && room < face_exclusions.size()){
			ArrayList<ArrayList<DoublePoint>> fff = opposing_face_exclusions.get(room);
			for(int ii = 0; ii < faces.size(); ii++){
				if(faces.get(ii).equals(face)){
					return fff.get(ii);
				}
			}
		}
		return null;
	}
	
	public void excludeRoom(int room){
		room_exclusions.add(room);
	}
	
	public void excludeFace(int room, DoublePoint face){
		if(room >= 0 && room < face_exclusions.size()){
			face_exclusions.get(room).add(face);
		}
	}
	
	public void excludeOpposingFace(int room, DoublePoint face, DoublePoint oface){
		if(room >= 0 && room < opposing_face_exclusions.size()){
			for(int ii = 0; ii < faces.size(); ii++){
				if(faces.get(ii).equals(face)){
					opposing_face_exclusions.get(room).get(ii).add(oface);
					if(opposing_face_exclusions.get(room).get(ii).size() == 4)
						face_exclusions.get(room).add(face);
					return;
				}
			}
		}
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

class DoublePoint{
	public Point p1, p2;
	
	public DoublePoint(Point p1, Point p2){
		if(p1.x < p2.x){
			this.p1 = p1;
			this.p2 = p2;
		} else if(p1.x > p2.x){
			this.p1 = p2;
			this.p2 = p1;
		} else if(p1.x == p2.x){
			if(p1.y < p2.y){
				this.p1 = p1;
				this.p2 = p2;
			}
			else{
				this.p1 = p2;
				this.p2 = p1;
			}
		}
	}
	
	public boolean equals(DoublePoint p){
		if(p1.x == p.p1.x && p1.y == p.p1.y && p2.x == p.p2.x && p2.y == p.p2.y)
			return true;
		return false;
	}
	
	public double getDistance(DoublePoint p){
		Point centerPoint1, centerPoint2;
		centerPoint1 = new Point((p1.x+p2.x)/2, (p1.y+p2.y)/2);
		centerPoint2 = new Point((p.p1.x + p.p2.x)/2, (p.p1.y+p.p2.y)/2);
		return Def.get_distance(centerPoint1, centerPoint2);
	}
}
