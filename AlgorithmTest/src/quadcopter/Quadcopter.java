package quadcopter;

//TODO: comments and control code
//TODO: Streamline
//TODO: The directions are all messed up, after testing need to go through this and fix everything properly
//TODO: redo tracing so it is in terms of quadcopter position
//TODO: use hash tables
//TODO: make sure that you dont go to a decision point if its already dead

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import defaults.Def;
import gui.GUI;
import map.Map;

public class Quadcopter {
	
	private Map map;
	private GUI gui;
	private BufferedWriter log;
	private int sensor_range;
	private int pos_xx, pos_yy;
	private Point nextMove;
	
	private ArrayList<Point> path;
	private ArrayList<Decision> decisions;
	
	private ArrayList<ArrayList<Integer>> internal_map;
	private int centerpoint_xx, centerpoint_yy;
	
	private boolean retrace;
	private int retrace_index;
	private Point retrace_point;
	private ArrayList<Point> retrace_path;
	int back_tracking;
	
	private ArrayList<Point> points_seen;
	
	public Quadcopter(BufferedWriter logger, Map map, GUI gui) throws IOException{
		this.map = map;
		this.log = logger;
		this.gui = gui;
		back_tracking = 0;
		pos_xx = 0;
		pos_yy = 0;
		nextMove = new Point(-1,-1);
		path = new ArrayList<Point>();
		retrace = false;
		retrace_point = new Point(-1,-1);
		retrace_index = 0;
		decisions = new ArrayList<Decision>();
		points_seen = new ArrayList<Point>();
		retrace_path = new ArrayList<Point>();
		
		
		if(Def.NUMBER_OF_SENSORS != 8 && Def.NUMBER_OF_SENSORS != 16){
			System.out.println("Number of sensors is incompatible.");
			System.exit(-1);
		}
		if(Def.SENSOR_RANGE_FT != 8){
			System.out.println("There is currently no protocol for different sensor ranges.");
			System.exit(-1);
		}
		
		sensor_range = Def.SENSOR_RANGE_FT/Def.FT_PER_SQUARE;
		internal_map = new ArrayList<ArrayList<Integer>>();
		for(int ii = 0; ii < (2*sensor_range + 1); ii++){
			internal_map.add(new ArrayList<Integer>());
			for(int jj = 0; jj < (2*sensor_range + 1); jj++){
				internal_map.get(ii).add(0);
			}
		}
		
		centerpoint_xx = sensor_range;
		centerpoint_yy = sensor_range;
		internal_map.get(centerpoint_xx).set(centerpoint_yy, Def.QUADCOPTER_CODE);
		
		Point p1 = map.find_start_position();
		if(p1.x != -1)
			set_start_position(p1.x, p1.y);
		else{
			System.out.println("Cannot obtain start position.");
			System.exit(-1);
		}
		
		gui.addInternalMap(internal_map);
	}
	
	public void set_start_position(int xx, int yy){
		pos_xx = xx;
		pos_yy = yy;
		if(map.update(xx, yy, Def.QUADCOPTER_CODE) == -1){
			System.out.println("Update fault");
			System.exit(-1);
		}
	}
	
	public void sense(){
		for(int ii = 0; ii < (2*sensor_range + 1); ii++){
			for(int jj = 0; jj < (2*sensor_range + 1); jj++)
				internal_map.get(ii).set(jj, 0);
		}
		internal_map.get(centerpoint_xx).set(centerpoint_yy, Def.QUADCOPTER_CODE);
		
		if(Def.NUMBER_OF_SENSORS == 16){
			for(int ii = 1; ii <= 16; ii++){
				boolean wall = false;
				for(int jj = 1; jj <= sensor_range; jj++){
					int type;
					if(wall == false){
						Point sensor_locus = Def.SENSOR_SCAN(new Point(pos_xx, pos_yy), jj, ii);
						type = map.get(sensor_locus.x, sensor_locus.y);
						if(type == -1)
							type = 1;
						if(type == 1)
							wall = true;
						if(type == 2)
							map.update(sensor_locus.x, sensor_locus.y, Def.CURRENTLY_SENSING_AREA_CODE);
					}
					else type = 0;
					
					Point internal_locus = Def.SENSOR_SCAN(new Point(centerpoint_xx, centerpoint_yy), jj, ii);
					internal_map.get(internal_locus.x).set(internal_locus.y,type);
					gui.update_i_square(internal_locus.x, internal_locus.y);
				}
			}
		}
	}
	
	public void movement_protocol(boolean dead_end) throws IOException{
		

		if(retrace == true && dead_end == false){
			if(retrace_path.size() == 0)
				movement_protocol(true);
			else{
				nextMove = retrace_path.get(0);
				retrace_path.remove(0);
			}
			/*
			if(retrace_point.equals(new Point(pos_xx, pos_yy)))
				movement_protocol(true);
			else{
				nextMove = path.get(retrace_index);
				retrace_index--;
			}
			*/
		} else{
			
			//basic movement protocol.
			//Checks internal map for 2 squares in each direction. If 1 is possible , it will move, if 2 are possible
			//then a decision point is made. THis point will be referred to in the case of a dead end
			//only have to check if you've been somewhere already at a decision point
			ArrayList<Boolean> list = new ArrayList<Boolean>();
			for(int ii = 0; ii < 4; ii++) list.add(false);
			
			//first check where you can go
			
			int sq1, sq2, sq3, sq4;
			//check up
			sq1 = internal_map.get(centerpoint_xx).get(centerpoint_yy - 2);
			sq2 = internal_map.get(centerpoint_xx + 1).get(centerpoint_yy - 2);
			sq3 = internal_map.get(centerpoint_xx - 1).get(centerpoint_yy - 2);
			sq4 = internal_map.get(centerpoint_xx).get(centerpoint_yy - 3);
			sq4 = 3;
			
			if(sq1 > 1 && sq1 < 9 && sq2 > 1 && sq2 < 9 && sq3 > 1 && sq3 < 9 && sq4 > 1 && sq4 < 9)
				list.set(0, true);
			//check down
			sq1 = internal_map.get(centerpoint_xx).get(centerpoint_yy + 2);
			sq2 = internal_map.get(centerpoint_xx + 1).get(centerpoint_yy + 2);
			sq3 = internal_map.get(centerpoint_xx - 1).get(centerpoint_yy + 2);
			sq4 = internal_map.get(centerpoint_xx).get(centerpoint_yy +3);
			sq4 = 3;
			
			if(sq1 > 1 && sq1 < 9 && sq2 > 1 && sq2 < 9 && sq3 > 1 && sq3 < 9 && sq4 > 1 && sq4 < 9)
				list.set(1, true);
			//check left
			sq1 = internal_map.get(centerpoint_xx-2).get(centerpoint_yy);
			sq2 = internal_map.get(centerpoint_xx - 2).get(centerpoint_yy +1);
			sq3 = internal_map.get(centerpoint_xx - 2).get(centerpoint_yy - 1);
			sq4 = internal_map.get(centerpoint_xx-3).get(centerpoint_yy);
			sq4 = 3;
			
			if(sq1 > 1 && sq1 < 9 && sq2 > 1 && sq2 < 9 && sq3 > 1 && sq3 < 9 && sq4 > 1 && sq4 < 9)
				list.set(2, true);
			//check right
			sq1 = internal_map.get(centerpoint_xx + 2).get(centerpoint_yy);
			sq2 = internal_map.get(centerpoint_xx + 2).get(centerpoint_yy + 1);
			sq3 = internal_map.get(centerpoint_xx +2).get(centerpoint_yy - 1);
			sq4 = internal_map.get(centerpoint_xx+3).get(centerpoint_yy);
			sq4 = 3;
			
			if(sq1 > 1 && sq1 < 9 && sq2 > 1 && sq2 < 9 && sq3 > 1 && sq3 < 9 && sq4 > 1 && sq4 < 9)
				list.set(3, true);
			
			//check where you came from
			if(path.size() != 0){
				int pre = get_direction(new Point(pos_xx, pos_yy), path.get(path.size()-1));
				switch(pre){
					case 0: list.set(0, false); break;
					case 1: list.set(1, false); break;
					case 2: list.set(2, false);	break;
					case 3: list.set(3, false);	break;
				}
			}
			
			
			//get number of possible directions
			int pos = 0;
			for(int ii = 0; ii < 4; ii++)
				if(list.get(ii) == true)
					pos++;
			
			System.out.println("pos = " + Integer.toString(pos));
			
			if(pos == 0 || dead_end == true){
				//dead end, need to retrace
				//TODO: retrace code
				
				if(decisions.size() == 0){
					System.out.println("All Done");
					map.print_stats();
					System.out.printf("Quadcopter Stats: \nTotal Steps = %d\nBacktracking steps = %d\n", path.size(), back_tracking);
					log.write(String.format("Quadcopter Stats: \nTotal Steps = %d\nBacktracking steps = %d\n", path.size(), back_tracking));
					log.close();
					BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in)); 
					stdin.readLine();
				}
				else{
					//first need to retrace to a decision point
					if(retrace == false){
						retrace = true;
						retrace_point = decisions.get(decisions.size()-1).decision_location;
						retrace_index = path.size()-1;
						create_retrace_path(retrace_point);
					} else{
						retrace = false;
						int dir = decisions.get(decisions.size()-1).get_next_direction();
						
						if(dir == -1){
							//all directions exhausted at this decision
							//remove it and dead end
							decisions.remove(decisions.size() - 1);
							movement_protocol(true);
						} else{
							switch(dir){
								case 0:	this.nextMove = new Point(pos_xx, pos_yy - 1); break;
								case 1:	this.nextMove = new Point(pos_xx, pos_yy + 1); break;
								case 2:	this.nextMove = new Point(pos_xx - 1, pos_yy); break;
								case 3:	this.nextMove = new Point(pos_xx + 1, pos_yy); break;
							}
						}	
					}
				}
			}
			else if(pos == 1){
				//only one direction, just move in that direction
				for(int ii = 0; ii < 4; ii++){
					if(list.get(ii) == true){
						switch(ii){
							case 0:	this.nextMove = new Point(pos_xx, pos_yy - 1); break;
							case 1:	this.nextMove = new Point(pos_xx, pos_yy + 1); break;
							case 2:	this.nextMove = new Point(pos_xx - 1, pos_yy); break;
							case 3:	this.nextMove = new Point(pos_xx + 1, pos_yy); break;
						}
						break;
					}
				}
			} else if(pos > 1){
				//decision point
				//TODO: Decision point code
				//first have to check if you have been here before
				//simply go throught the path and check if the point is the same & check if you have been 1 away from the current point
				//this may give large errors, but it is a start
				boolean been_here = false;
				for(int ii = decisions.size()-1; ii >= 0; ii--){
					//if(Math.abs(pos_xx - decisions.get(ii).decision_location.x) <= 1 && Math.abs(pos_yy - decisions.get(ii).decision_location.y) <= 1){
					if(pos_xx == path.get(ii).x && pos_yy == path.get(ii).y){
						been_here = true;
						break;
					}
				}
				
				if(been_here){
					//youve been here before
					//rerun movement protocol as a dead end
					movement_protocol(true);
				} else{	
					Decision dec = new Decision(path.size()-1, new Point(pos_xx, pos_yy), list.get(0), list.get(1), list.get(2), list.get(3)); 
					decisions.add(dec);
					int dir = dec.get_next_direction();
					
					if(dir == -1){
						//all directions exhausted at this decision
						//remove it and dead end
						decisions.remove(decisions.size() - 1);
						movement_protocol(true);
					} else{
						switch(dir){
							case 0:	this.nextMove = new Point(pos_xx, pos_yy - 1); break;
							case 1:	this.nextMove = new Point(pos_xx, pos_yy + 1); break;
							case 2:	this.nextMove = new Point(pos_xx - 1, pos_yy); break;
							case 3:	this.nextMove = new Point(pos_xx + 1, pos_yy); break;
						}
					}		
				}
			}
		}
	}
	
	public void move(){
		//been there done that
		map.update(pos_xx, pos_yy, Def.PATH_CODE);
		System.out.println("nextMove = (" + Integer.toString(nextMove.x) + "," + Integer.toString(nextMove.y) + ")");
		System.out.println("Current Position = (" + Integer.toString(pos_xx)  + "," + Integer.toString(pos_yy)+ ")");
		/*int dir = get_direction(new Point(pos_xx,pos_yy), nextMove);
		System.out.println("dir = " + Integer.toString(dir));
		switch(dir){
			case 0: map.update(pos_xx, pos_yy-1, Def.PATH_CODE); path.add(new Point(pos_xx, pos_yy-1)); break;
			case 1: map.update(pos_xx, pos_yy+1, Def.PATH_CODE); path.add(new Point(pos_xx, pos_yy+1)); break;
			case 2: map.update(pos_xx-1, pos_yy, Def.PATH_CODE); path.add(new Point(pos_xx-1, pos_yy)); break;
			case 3: map.update(pos_xx+1, pos_yy, Def.PATH_CODE); path.add(new Point(pos_xx+1, pos_yy)); break;
		}
		*/
		
		/************ Kill crap decision points ***********/
		for(int ii = decisions.size()-1; ii >= 0; ii--){
			Point dec_point = decisions.get(ii).decision_location;
			if(Math.abs(nextMove.x - dec_point.x) <= 2 && Math.abs(nextMove.y - dec_point.y) <= 2){
				if(dec_point.x == nextMove.x){
					if(dec_point.y < nextMove.y && decisions.get(ii).directions_not_taken.get(1) == true){
						decisions.get(ii).directions_not_taken.set(1, false);
						boolean found_alter = false;
						for(int jj = 0; jj < 4; jj++)
							if(decisions.get(ii).directions_not_taken.get(jj) == true)
								found_alter = true;
						if(!found_alter)
							decisions.remove(ii);
					}
					else if(dec_point.y > nextMove.y && decisions.get(ii).directions_not_taken.get(0) == true){
						decisions.get(ii).directions_not_taken.set(0, false);
						boolean found_alter = false;
						for(int jj = 0; jj < 4; jj++)
							if(decisions.get(ii).directions_not_taken.get(jj) == true)
								found_alter = true;
						if(!found_alter)
							decisions.remove(ii);
					}
				} else if(dec_point.y == nextMove.y){
					if(dec_point.x < nextMove.x && decisions.get(ii).directions_not_taken.get(3) == true){
						decisions.get(ii).directions_not_taken.set(3, false);
						boolean found_alter = false;
						for(int jj = 0; jj < 4; jj++)
							if(decisions.get(ii).directions_not_taken.get(jj) == true)
								found_alter = true;
						if(!found_alter)
							decisions.remove(ii);
					}
					else if(dec_point.x > nextMove.x && decisions.get(ii).directions_not_taken.get(2) == true){
						decisions.get(ii).directions_not_taken.set(2, false);
						boolean found_alter = false;
						for(int jj = 0; jj < 4; jj++)
							if(decisions.get(ii).directions_not_taken.get(jj) == true)
								found_alter = true;
						if(!found_alter)
							decisions.remove(ii);
					}
				}
			}
		}
		
		map.update(nextMove.x, nextMove.y, Def.QUADCOPTER_CODE);
		
		path.add(new Point(pos_xx, pos_yy));
		pos_xx = nextMove.x;
		pos_yy = nextMove.y;
		
		boolean found = false;
		for(int ii = 0; ii < points_seen.size(); ii++){
			if(nextMove.equals(points_seen.get(ii))){
				found = true;
				back_tracking++;
				break;
			}				
		}
		if(!found)
			points_seen.add(nextMove);
		
	}
	
	//gets the direction you would go from the current point to the previous point so you can discount it
	//0 - up, 1 - down, 2 - left, 3 - right
	//this currently only works for 1d translation (preferably 2)
	private int get_direction(Point current, Point previous){
		if(current.x == previous.x){
			if(current.y < previous.y)
				return 1;	//down
			else
				return 0;
		} else if(current.y == previous.y){
			if(current.x < previous.x)
				return 3;
			else
				return 2;
		}
		return -1;
	}
	
	public void printPath(){
		System.out.println("\nPath:");
		for(int ii = 0; ii < path.size(); ii++){
			System.out.printf("%4d (%d,%d)\n", ii+1, path.get(ii).x, path.get(ii).y);
		}
		System.out.printf("%4c (%d,%d)\n\n", 'c', pos_xx, pos_yy);
	}
	
	private void create_retrace_path(Point end){
		retrace_path.clear();
		
		/*
		Point cur_point = path.get(path.size()-1);
		int index = path.size()-1;
		while(!cur_point.equals(end)){
			boolean found = false;
			for(int ii = 0; ii < retrace_path.size(); ii++)
				if(cur_point.equals(retrace_path.get(ii))){
						found = true;
						break;
				}
			if(found == false)
				retrace_path.add(cur_point);
			cur_point = path.get(--index);
		}
		retrace_path.add(cur_point);
		*/
		
		
		/***************************/
		ArrayList<rpath> paths1 = new ArrayList<rpath>();
		
		boolean path_found = false;
		paths1.add(new rpath());
		paths1.get(0).path.add(new Point(pos_xx, pos_yy));
		
		while(!path_found){
			for(int ii = paths1.size() - 1; ii >= 0; ii--){
				boolean point_added = false;
				System.out.println("ii = " + Integer.toString(ii));
				Point curr_point = paths1.get(ii).path.get(paths1.get(ii).path.size()-1);
				if(point_exists(new Point(curr_point.x, curr_point.y+1)) && !paths1.get(ii).point_exists(new Point(curr_point.x, curr_point.y+1))){
					point_added = true;
					paths1.get(ii).path.add(new Point(curr_point.x, curr_point.y+1));
					if(end.equals(new Point(curr_point.x, curr_point.y+1))){
						path_found = true;
						retrace_path = paths1.get(ii).path;
						break;
					}
				}
				
				if(point_exists(new Point(curr_point.x, curr_point.y-1)) && !paths1.get(ii).point_exists(new Point(curr_point.x, curr_point.y-1))){
					if(point_added){
						rpath new_rpath = new rpath();
						for(int jj = 0; jj < paths1.get(ii).path.size()-1; jj++)
							new_rpath.path.add(paths1.get(ii).path.get(jj));
						new_rpath.path.add(new Point(curr_point.x, curr_point.y-1));
						paths1.add(new_rpath);
						if(end.equals(new Point(curr_point.x, curr_point.y-1))){
							path_found = true;
							retrace_path = paths1.get(ii).path;
							break;
						}
					}
					else{
						point_added = true;
						paths1.get(ii).path.add(new Point(curr_point.x, curr_point.y-1));
						if(end.equals(new Point(curr_point.x, curr_point.y-1))){
							path_found = true;
							retrace_path = paths1.get(ii).path;
							break;
						}
					}
				}
				
				if(point_exists(new Point(curr_point.x-1, curr_point.y)) && !paths1.get(ii).point_exists(new Point(curr_point.x-1, curr_point.y))){
					if(point_added){
						rpath new_rpath = new rpath();
						for(int jj = 0; jj < paths1.get(ii).path.size()-1; jj++)
							new_rpath.path.add(paths1.get(ii).path.get(jj));
						new_rpath.path.add(new Point(curr_point.x-1, curr_point.y));
						paths1.add(new_rpath);
						if(end.equals(new Point(curr_point.x-1, curr_point.y))){
							path_found = true;
							retrace_path = paths1.get(ii).path;
							break;
						}
					}
					else{
						point_added = true;
						paths1.get(ii).path.add(new Point(curr_point.x-1, curr_point.y));
						if(end.equals(new Point(curr_point.x-1, curr_point.y))){
							path_found = true;
							retrace_path = paths1.get(ii).path;
							break;
						}
					}
				}
				
				if(point_exists(new Point(curr_point.x+1, curr_point.y)) && !paths1.get(ii).point_exists(new Point(curr_point.x+1, curr_point.y))){
					if(point_added){
						rpath new_rpath = new rpath();
						for(int jj = 0; jj < paths1.get(ii).path.size()-1; jj++)
							new_rpath.path.add(paths1.get(ii).path.get(jj));
						new_rpath.path.add(new Point(curr_point.x+1, curr_point.y));
						paths1.add(new_rpath);
						if(end.equals(new Point(curr_point.x+1, curr_point.y))){
							path_found = true;
							retrace_path = paths1.get(ii).path;
							break;
						}
					}
					else{
						point_added = true;
						paths1.get(ii).path.add(new Point(curr_point.x+1, curr_point.y));
						if(end.equals(new Point(curr_point.x+1, curr_point.y))){
							path_found = true;
							retrace_path = paths1.get(ii).path;
							break;
						}
					}
				}
				if(!point_added)
					paths1.remove(ii);
			}
		}
		
	}
	
	private boolean point_exists(Point pp){
		for(int ii = 0; ii < points_seen.size(); ii++){
			if(pp.equals(points_seen.get(ii)))
				return true;
		}
		return false;
	}
}

class Decision{
	public int path_index;
	public Point decision_location;
	public ArrayList<Boolean> directions_not_taken;
	
	public Decision(int path_index, Point decisions_location, boolean up, boolean down, boolean left, boolean right){
		this.path_index = path_index;
		this.decision_location = decisions_location;
		this.directions_not_taken = new ArrayList<Boolean>();
		directions_not_taken.add(up);
		directions_not_taken.add(down);
		directions_not_taken.add(left);
		directions_not_taken.add(right);
	}
	
	public int get_next_direction(){
		for(int ii = 0; ii < directions_not_taken.size(); ii++){
			if(directions_not_taken.get(ii) == true){
				directions_not_taken.set(ii, false);
				return ii;
			}
		}
		return -1;
	}
}

class rpath{
	public ArrayList<Point> path;
	public rpath(){
		path = new ArrayList<Point>();
	}
	public boolean point_exists(Point p){
		for(int ii = 0; ii < path.size(); ii++)
			if(p.equals(path.get(ii)))
				return true;
		return false;
	}
}