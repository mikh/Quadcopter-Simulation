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
		Def.output(log, "Map construction initiated\r\n");
		grid = new ArrayList<ArrayList<Integer>>();
		log = log_file;
		
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
		log.write("Generating map...\r\n");
		Random rand = new Random(seed);
		
		//first create border
		for(int ii = 0; ii < grid.size(); ii++){
			if(ii == 0 || ii == grid.size() - 1){
				for(int jj = 0; jj < grid.get(ii).size(); jj++)
					grid.get(ii).set(jj, 1);
			}
			else{
				grid.get(ii).set(0, 1);
				grid.get(ii).set(grid.get(ii).size()-1, 1);
			}
		}
		
		//Create point of entry
		Point pos1 = new Point(0,0), pos2 = new Point(0,0);
		int direction = rand.nextInt(4), position;	//0 - top, 1 - left, 2 - down, 3 - right
		if(direction == 0 || direction == 2){
			position = rand.nextInt(grid.size() - 2 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)) + 1;
			if(direction == 0){
				for(int ii = 0; ii < (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE); ii++){
					grid.get(position+ii).set(0, 2);
				}
				pos1 = new Point(position, 0);
				pos2 = new Point(position + (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE), 0);
			} else{
				for(int ii = 0; ii < (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE); ii++){
					grid.get(position + ii).set(grid.get(position + ii).size()-1, 2);
				}
				pos1 = new Point(position, grid.get(position).size()-1);
				pos2 = new Point(position+ (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE), grid.get(position+ (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)).size()-1);
			}
		}
		else if(direction == 1 || direction == 3){
			if(direction == 1){
				position = rand.nextInt(grid.get(grid.size()-1).size() - 2 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)) + 1;
				for(int ii = 0; ii < (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE); ii++){
					grid.get(grid.size()-1).set(position+ii, 2);
				}
				pos1 = new Point(grid.get(position).size()-1, position);
				pos2 = new Point(grid.get(position + (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)).size()-1, position + (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE));
			} else{
				position = rand.nextInt(grid.get(0).size() - 2 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)) + 1;
				for(int ii = 0; ii < (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE); ii++){
					grid.get(0).set(position+ii, 2);
				}
				pos1 = new Point(0, position);
				pos2 = new Point(0, position+(Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE));
			}
		}
		
		
		//generateha floor plan
		//at each segment in the hallway, there is some chance 
		
		ArrayList<hallway> hallway_stack = new ArrayList<hallway>();
		ArrayList<room> room_list = new ArrayList<room>();
		
		int hallway_increments = 0;
		hallway_stack.add(new hallway(pos1, pos2, direction));
		int hallL = Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE;
		int grid_x = grid.size(), grid_y = grid.get(0).size();
		
		while(hallway_increments < Def.HALLWAY_INCREMENTS){
			hallway_increments++;
						
			for(int ii = 0; ii < hallway_stack.size(); ii++){
				int xx_s = hallway_stack.get(ii).start.x, yy_s = hallway_stack.get(ii).start.y, xx_e = hallway_stack.get(ii).end.x, yy_e = hallway_stack.get(ii).end.y;
				int action = 0;	//0 = unassigned, 1 = turn left, 2 = turn right, 3 = fork left, 4 = fork right, 5 = fork both, 6 = dead end
				
				switch(hallway_stack.get(ii).direction){
					case 0:		
						action = 0;
						if(yy_s == grid_y - 2 - hallL){ //you are hitting a wall soon. So turn or dead end it.
							double prob = rand.nextDouble();
							
							if(hallway_stack.size() == 1){//if you're the only hallway left and you can't dead-end
								if(xx_e >= (grid_x - 1 - hallL)) action = 1;		//turn right if there is a wall to the left
								else if(xx_s <= hallL)	action = 2; 				//if there is a wall to the right turn right
								else if(prob > 0.5) action = 1; //turn left
								else action = 2;  //turn right
							} else{		//can dead end
								if(prob > 0.66) action = 6;  //dead end
								else if( prob > 0.33){	//turn left
									action = 1;
									if(xx_s <= hallL) action = 2;
									
								} else{ //turn right
									action = 2;
									if(xx_e >= (grid_x - 1 - hallL)) action = 1;
								}
							}				
						}
						
						if(action == 0){
							double prob = rand.nextDouble();
							if(prob < 0.01){
								//dead end
								if(hallway_stack.size() == 1)
									action = 0;
								else
									action  = 6;
							} /*else if (prob < 0.06){
								//fork
								if(hallway_stack.get(ii).end.x >= (grid.size() - 1 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 4;
								else if((hallway_stack.get(ii).start.x <= (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 3;
								else if(prob < 0.011)
									action = 5;
								else if(prob < 0.035)
									action = 3;
								else
									action = 4;
							}*/ else if(prob < 0.11){
								//turn
								if(hallway_stack.get(ii).end.x >= (grid.size() - 1 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 2;
								else if((hallway_stack.get(ii).start.x <= (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 1;
								else if(prob < 0.085)
									action = 1;
								else
									action = 2;
							}
							else if(prob < 0.21){
								//room
								//TODO:make room code
								action = 0;
							}
						}
						
						
						//generation
						switch(action){
							case 0:	//move forward
								for(int kk = 0; kk < hallL; kk++){
									grid.get(xx_s+kk).set(yy_s+1, 2);
								}
								hallway_stack.get(ii).start.y = yy_s+1;
								hallway_stack.get(ii).end.y = yy_e+1;
								break;
							case 1:	//turn left
								for(int jj = -1; jj < hallL; jj++){
									for(int kk = 1; kk <= hallL; kk++){
										grid.get(xx_s+jj).set(yy_s+kk, 2);
									}
								}
								
								hallway_stack.get(ii).start.x = xx_s - 1;
								hallway_stack.get(ii).start.y = yy_s + 1;
								hallway_stack.get(ii).end.x = xx_s - 1;
								hallway_stack.get(ii).end.y = yy_s +  hallL;
								hallway_stack.get(ii).direction = 1;
								break;
							case 2:	//turn right
								for(int jj = 0; jj <= hallL; jj++){
									for(int kk = 1; kk <= hallL; kk++){
										grid.get(xx_s+jj).set(yy_s+kk, 2);
									}
								}
								
								hallway_stack.get(ii).start.x = xx_s + hallL;
								hallway_stack.get(ii).start.y = yy_s + 1;
								hallway_stack.get(ii).end.x = xx_s + hallL;
								hallway_stack.get(ii).end.y = yy_s + hallL;
								hallway_stack.get(ii).direction = 3;
								break;
						}
						
					break;
					case 1:		
						action = 0;
						if(xx_s == 1 + hallL){ //you are hitting a wall soon. So turn or dead end it.
							double prob = rand.nextDouble();
							
							if(hallway_stack.size() == 1){//if you're the only hallway left and you can't dead-end
								if(yy_s <=  hallL) action = 1;		//turn right if there is a wall to the left
								else if(yy_e >= (grid_y - 1 - hallL))	action = 2; 				//if there is a wall to the right turn right
								else if(prob > 0.5) action = 1; //turn left
								else action = 2;  //turn right
							} else{		//can dead end
								if(prob > 0.66) action = 6;  //dead end
								else if( prob > 0.33){	//turn left
									action = 1;
									if(yy_e >= (grid_y - 1 - hallL))	action = 2;
									
								} else{ //turn right
									action = 2;
									if(yy_s <=  hallL) action = 1;
								}
							}				
						}
						
						if(action == 0){
							double prob = rand.nextDouble();
							if(prob < 0.01){
								//dead end
								if(hallway_stack.size() == 1)
									action = 0;
								else
									action  = 6;
							} /*else if (prob < 0.06){
								//fork
								if(hallway_stack.get(ii).end.x >= (grid.size() - 1 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 4;
								else if((hallway_stack.get(ii).start.x <= (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 3;
								else if(prob < 0.011)
									action = 5;
								else if(prob < 0.035)
									action = 3;
								else
									action = 4;
							}*/ else if(prob < 0.11){
								//turn
								if(hallway_stack.get(ii).end.x >= (grid.size() - 1 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 2;
								else if((hallway_stack.get(ii).start.x <= (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 1;
								else if(prob < 0.085)
									action = 1;
								else
									action = 2;
							}
							else if(prob < 0.21){
								//room
								//TODO:make room code
								action = 0;
							}
						}
						
						
						//generation
						switch(action){
							case 0:	//move forward
								for(int kk = 0; kk < hallL; kk++){
									grid.get(xx_s-1).set(yy_s+kk, 2);
								}
								hallway_stack.get(ii).start.x = xx_s-1;
								hallway_stack.get(ii).end.x = xx_e-1;
								break;
							case 1:	//turn left
								for(int jj = -1*(hallL); jj < 0; jj++){
									for(int kk = 0; kk <= hallL; kk++){
										grid.get(xx_s+jj).set(yy_s+kk, 2);
									}
								}
								
								hallway_stack.get(ii).start.x = xx_s - hallL;
								hallway_stack.get(ii).start.y = yy_s + hallL;
								hallway_stack.get(ii).end.x = xx_s - 1;
								hallway_stack.get(ii).end.y = yy_s + hallL;
								hallway_stack.get(ii).direction = 0;
								break;
							case 2:	//turn right
								for(int jj = -1*hallL; jj < 0; jj++){
									for(int kk = -1; kk < hallL; kk++){
										grid.get(xx_s+jj).set(yy_s+kk, 2);
									}
								}
								
								hallway_stack.get(ii).start.x = xx_s - hallL;
								hallway_stack.get(ii).start.y = yy_s + 1;
								hallway_stack.get(ii).end.x = xx_s - 1;
								hallway_stack.get(ii).end.y = yy_s + 1;
								hallway_stack.get(ii).direction = 2;
								break;
						}
						
					break;
					case 2:		
						action = 0;
						if(yy_s == hallL + 1){ //you are hitting a wall soon. So turn or dead end it.
							double prob = rand.nextDouble();
							
							if(hallway_stack.size() == 1){//if you're the only hallway left and you can't dead-end
								if(xx_e >= (grid_x - 1 - hallL)) action = 1;		//turn right if there is a wall to the left
								else if(xx_s <= hallL)	action = 2; 				//if there is a wall to the right turn right
								else if(prob > 0.5) action = 1; //turn left
								else action = 2;  //turn right
							} else{		//can dead end
								if(prob > 0.66) action = 6;  //dead end
								else if( prob > 0.33){	//turn left
									action = 1;
									if(xx_s <= hallL) action = 2;
									
								} else{ //turn right
									action = 2;
									if(xx_e >= (grid_x - 1 - hallL)) action = 1;
								}
							}				
						}
						
						if(action == 0){
							double prob = rand.nextDouble();
							if(prob < 0.01){
								//dead end
								if(hallway_stack.size() == 1)
									action = 0;
								else
									action  = 6;
							} /*else if (prob < 0.06){
								//fork
								if(hallway_stack.get(ii).end.x >= (grid.size() - 1 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 4;
								else if((hallway_stack.get(ii).start.x <= (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 3;
								else if(prob < 0.011)
									action = 5;
								else if(prob < 0.035)
									action = 3;
								else
									action = 4;
							}*/ else if(prob < 0.11){
								//turn
								if(hallway_stack.get(ii).end.x >= (grid.size() - 1 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 2;
								else if((hallway_stack.get(ii).start.x <= (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 1;
								else if(prob < 0.085)
									action = 1;
								else
									action = 2;
							}
							else if(prob < 0.21){
								//room
								//TODO:make room code
								action = 0;
							}
						}
						
						
						//generation
						switch(action){
							case 0:	//move forward
								for(int kk = 0; kk < hallL; kk++){
									grid.get(xx_s+kk).set(yy_s-1, 2);
								}
								hallway_stack.get(ii).start.y = yy_s-1;
								hallway_stack.get(ii).end.y = yy_e-1;
								break;
							case 1:	//turn left
								for(int jj = -1; jj < hallL; jj++){
									for(int kk = -hallL-1; kk < 0; kk++){
										grid.get(xx_s+jj).set(yy_s+kk, 2);
									}
								}
								
								hallway_stack.get(ii).start.x = xx_s - 1;
								hallway_stack.get(ii).start.y = yy_s - hallL;
								hallway_stack.get(ii).end.x = xx_s - 1;
								hallway_stack.get(ii).end.y = yy_s - 1;
								hallway_stack.get(ii).direction = 1;
								break;
							case 2:	//turn right
								for(int jj = 0; jj <= hallL; jj++){
									for(int kk = -hallL; kk < 0; kk++){
										grid.get(xx_s+jj).set(yy_s+kk, 2);
									}
								}
								
								hallway_stack.get(ii).start.x = xx_s + hallL;
								hallway_stack.get(ii).start.y = yy_s - hallL;
								hallway_stack.get(ii).end.x = xx_s + hallL;
								hallway_stack.get(ii).end.y = yy_s - 1;
								hallway_stack.get(ii).direction = 3;
								break;
						}
						
					break;
					case 3:		
						action = 0;
						if(xx_s == grid_x - 2 - hallL){ //you are hitting a wall soon. So turn or dead end it.
							double prob = rand.nextDouble();
							
							if(hallway_stack.size() == 1){//if you're the only hallway left and you can't dead-end
								if(yy_s <=  hallL) action = 1;		//turn right if there is a wall to the left
								else if(yy_e >= (grid_y - 1 - hallL))	action = 2; 				//if there is a wall to the right turn right
								else if(prob > 0.5) action = 1; //turn left
								else action = 2;  //turn right
							} else{		//can dead end
								if(prob > 0.66) action = 6;  //dead end
								else if( prob > 0.33){	//turn left
									action = 1;
									if(yy_e >= (grid_y - 1 - hallL))	action = 2;
									
								} else{ //turn right
									action = 2;
									if(yy_s <=  hallL) action = 1;
								}
							}				
						}
						
						if(action == 0){
							double prob = rand.nextDouble();
							if(prob < 0.01){
								//dead end
								if(hallway_stack.size() == 1)
									action = 0;
								else
									action  = 6;
							} /*else if (prob < 0.06){
								//fork
								if(hallway_stack.get(ii).end.x >= (grid.size() - 1 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 4;
								else if((hallway_stack.get(ii).start.x <= (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 3;
								else if(prob < 0.011)
									action = 5;
								else if(prob < 0.035)
									action = 3;
								else
									action = 4;
							}*/ else if(prob < 0.11){
								//turn
								if(hallway_stack.get(ii).end.x >= (grid.size() - 1 - (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 2;
								else if((hallway_stack.get(ii).start.x <= (Def.HALLWAY_LENGTH_FT/Def.FT_PER_SQUARE)))
									action = 1;
								else if(prob < 0.085)
									action = 1;
								else
									action = 2;
							}
							else if(prob < 0.21){
								//room
								//TODO:make room code
								action = 0;
							}
						}
						
						
						//generation
						switch(action){
							case 0:	//move forward
								for(int kk = 0; kk < hallL; kk++){
									grid.get(xx_s+1).set(yy_s+kk, 2);
								}
								hallway_stack.get(ii).start.x = xx_s+1;
								hallway_stack.get(ii).end.x = xx_e+1;
								break;
							case 1:	//turn left
								for(int jj = 1; jj <= hallL; jj++){
									for(int kk = 0; kk <= hallL; kk++){
										grid.get(xx_s+jj).set(yy_s+kk, 2);
									}
								}
								
								hallway_stack.get(ii).start.x = xx_s + 1;
								hallway_stack.get(ii).start.y = yy_s + hallL;
								hallway_stack.get(ii).end.x = xx_s + hallL;
								hallway_stack.get(ii).end.y = yy_s + hallL;
								hallway_stack.get(ii).direction = 0;
								break;
							case 2:	//turn right
								for(int jj = 1; jj <= hallL; jj++){
									for(int kk = -1; kk < hallL; kk++){
										grid.get(xx_s+jj).set(yy_s+kk, 2);
									}
								}
								
								hallway_stack.get(ii).start.x = xx_s + 1;
								hallway_stack.get(ii).start.y = yy_s - 1;
								hallway_stack.get(ii).end.x = xx_s + hallL;
								hallway_stack.get(ii).end.y = yy_s - 1;
								hallway_stack.get(ii).direction = 0;
						}
						
					break;				
				}
			}
		}
		
		
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
