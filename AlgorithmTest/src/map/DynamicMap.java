package map;

import gui.full_grid;

import java.awt.Point;
import java.util.ArrayList;

import defaults.Def;

public class DynamicMap extends ProtoMap
{
	private ArrayList<ArrayList<Integer>> grid;
	private int grid_x, grid_y;
	private full_grid gui;
	private Point quadcopter_position = new Point(-1,-1);
	
	/***** INIT *****/
		public DynamicMap(){
			grid = new ArrayList<ArrayList<Integer>>();
			for(int ii = 0; ii < Def.DYNAMIC_MAP_INITIAL_SIZE_X; ii++){
				ArrayList<Integer> row = new ArrayList<Integer>();
				for(int jj = 0; jj < Def.DYNAMIC_MAP_INITIAL_SIZE_Y; jj++){
					row.add(Def.UNASSIGNED_CODE);
				}
				grid.add(row);
			}
			quadcopter_position = new Point(Def.DYNAMIC_MAP_INITIAL_SIZE_X/2, Def.DYNAMIC_MAP_INITIAL_SIZE_Y/2);
			getGridDimensions();
		}
		
		private void getGridDimensions(){
			grid_x = grid.size();
			grid_y = grid.get(0).size();
		}
	/***** END INIT *****/
	
	/***** MAP CONSTRUCTION *****/
		public void insertSensorMap(ArrayList<ArrayList<Integer>> map){
			//check if need to resize
			getGridDimensions();
			int x_plus = 0, x_minus = 0, y_plus = 0, y_minus = 0;
			if(quadcopter_position.x + (Def.SENSOR_MAP_QUADCOPTER_POSITION.x + 1) > grid_x) x_plus = quadcopter_position.x + (Def.SENSOR_MAP_QUADCOPTER_POSITION.x + 1) - grid_x;
			if(quadcopter_position.x - (Def.SENSOR_MAP_QUADCOPTER_POSITION.x + 1) < 0) x_minus = -1*(quadcopter_position.x - (Def.SENSOR_MAP_QUADCOPTER_POSITION.x + 1));
			if(quadcopter_position.y + (Def.SENSOR_MAP_QUADCOPTER_POSITION.y + 1) > grid_y) y_plus = quadcopter_position.y + (Def.SENSOR_MAP_QUADCOPTER_POSITION.y + 1) - grid_y;
			if(quadcopter_position.y - (Def.SENSOR_MAP_QUADCOPTER_POSITION.y + 1) < 0) y_minus = -1*(quadcopter_position.y - (Def.SENSOR_MAP_QUADCOPTER_POSITION.y + 1));
			resize(x_plus, x_minus, y_plus, y_minus);
			
			Point offset = new Point(quadcopter_position.x, quadcopter_position.y);
			offset.x -= Def.SENSOR_MAP_QUADCOPTER_POSITION.x;
			offset.y -= Def.SENSOR_MAP_QUADCOPTER_POSITION.y;
			
			for(int ii = 0; ii < map.size(); ii++){
				for(int jj = 0; jj < map.get(ii).size(); jj++){
					if(map.get(ii).get(jj) != Def.UNASSIGNED_CODE){
						if(map.get(ii).get(jj) == Def.MOVABLE_AREA_CODE){
							int type = grid.get(ii + offset.x).get(jj + offset.y);
							if(type == Def.WALL_CODE || type == Def.UNASSIGNED_CODE)
								update(ii+offset.x, jj+offset.y, Def.MOVABLE_AREA_CODE);
						} else{
							update(ii + offset.x, jj + offset.y, map.get(ii).get(jj));
						}
					}
				}
			}
		}
		
		public void resize(int x_plus, int x_minus, int y_plus, int y_minus){
			if(x_plus < 0 || x_minus < 0 || y_plus < 0 || y_minus < 0){
				while(true) System.out.println("Negative resize!");
			}
			if(x_plus != 0){
				ArrayList<ArrayList<Integer>> new_grid = new ArrayList<ArrayList<Integer>>();
				for(int ii = 0; ii < grid_x; ii++){
					ArrayList<Integer> row = new ArrayList<Integer>();
					for(int jj = 0; jj < grid_y; jj++)
						row.add(grid.get(ii).get(jj));
					new_grid.add(row);
				}
				for(int ii = grid_x; ii < grid_x + x_plus; ii++){
					ArrayList<Integer> row = new ArrayList<Integer>();
					for(int jj = 0; jj < grid_y; jj++)
						row.add(Def.UNASSIGNED_CODE);
					new_grid.add(row);
				}
				grid = new_grid;
			}
			if(x_minus != 0){
				ArrayList<ArrayList<Integer>> new_grid = new ArrayList<ArrayList<Integer>>();
				for(int ii = 0; ii < x_minus; ii++){
					ArrayList<Integer> row = new ArrayList<Integer>();
					for(int jj = 0; jj < grid_y; jj++){
						row.add(Def.UNASSIGNED_CODE);
					}
					new_grid.add(row);
				}
				for(int ii = 0; ii < grid_x; ii++){
					ArrayList<Integer> row = new ArrayList<Integer>();
					for(int jj = 0; jj < grid_y; jj++)
						row.add(grid.get(ii).get(jj));
					new_grid.add(row);
				}
				quadcopter_position.x += x_minus;
				grid = new_grid;
			}
			if(y_plus != 0){
				ArrayList<ArrayList<Integer>> new_grid = new ArrayList<ArrayList<Integer>>();
				for(int ii = 0; ii < grid_x; ii++){
					ArrayList<Integer> row = new ArrayList<Integer>();
					for(int jj = 0; jj < grid_y; jj++)
						row.add(grid.get(ii).get(jj));
					for(int jj = 0; jj < y_plus; jj++)
						row.add(Def.UNASSIGNED_CODE);						
					new_grid.add(row);
				}
				grid = new_grid;
			}
			if(y_minus != 0){
				ArrayList<ArrayList<Integer>> new_grid = new ArrayList<ArrayList<Integer>>();
				for(int ii = 0; ii < grid_x; ii++){
					ArrayList<Integer> row = new ArrayList<Integer>();
					for(int jj = 0; jj < y_minus; jj++)
						row.add(Def.UNASSIGNED_CODE);	
					for(int jj = 0; jj < grid_y; jj++)
						row.add(grid.get(ii).get(jj));
					
					new_grid.add(row);
				}
				quadcopter_position.y += y_minus;
				grid = new_grid;
			}
			if(x_plus != 0 || x_minus != 0 || y_plus != 0 || y_minus != 0){
				this.getGridDimensions();
				gui.recreate(grid_x*Def.FT_PER_SQUARE, grid_y*Def.FT_PER_SQUARE);
			}
		}
	
		public Point move(int direction, int distance){
			getGridDimensions();
			Point savePos = new Point(quadcopter_position.x, quadcopter_position.y);
			Point curPos = new Point(quadcopter_position.x, quadcopter_position.y);
			if(direction == Def.UP){
				curPos.x -= distance;
			} else if(direction == Def.DOWN)
				curPos.x += distance;
			else if(direction == Def.RIGHT)
				curPos.y += distance;
			else if(direction == Def.LEFT)
				curPos.y -= distance;
			
			boolean fail = false;
			if(curPos.x < 0 || curPos.x > grid_x)
				fail = true;
			else if(curPos.y < 0 || curPos.y > grid_y)
				fail = true;
			else if(grid.get(curPos.x).get(curPos.y) == Def.WALL_CODE)
				fail = true;
			
			if(fail){
				System.out.println("YOU CRASHED!");
			}
			else{
				update(quadcopter_position.x, quadcopter_position.y, Def.SENSED_AND_SEARCHED_AREA_CODE);
				quadcopter_position = curPos;
				update(curPos.x, curPos.y, Def.QUADCOPTER_CODE);
			}
			return new Point(Math.abs(savePos.x - curPos.x), Math.abs(savePos.y - curPos.y));
		}
	/***** END MAP CONSTRUCTION *****/
		
	@Override public int get(int xx, int yy) { return grid.get(xx).get(yy); }
	
	public void clear(){
		for(int ii = 0; ii < grid.size(); ii++){
			for(int jj = 0; jj < grid.get(ii).size(); jj++){
				try{
					update(ii,jj, Def.UNASSIGNED_CODE);
				} catch(Exception e){
					System.out.println("Error");
				}
			}
		}
	}
	
	
	
	/***** GUI CODE *****/
		private void update(int xx, int yy, int type){
			grid.get(xx).set(yy, type);
			gui.update_square(xx, yy);
		}
		
		@Override public void attachGUI(full_grid gui) { this.gui = gui; }
	/***** END GUI CODE *****/
}
