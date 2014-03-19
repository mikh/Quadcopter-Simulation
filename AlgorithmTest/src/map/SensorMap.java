package map;

import java.awt.Point;
import java.util.ArrayList;

import sensor.Sensor;
import defaults.Def;
import gui.full_grid;

public class SensorMap extends ProtoMap{
	private ArrayList<ArrayList<Integer>> grid;
	private full_grid gui;
	private ArrayList<Sensor> sensor_list;
	private double direction;
	private Map map;
	
	/***** INIT ****/
		public SensorMap(){
			grid = new ArrayList<ArrayList<Integer>>();
			for(int ii = 0; ii < Def.SENSOR_MAP_SIZE_X/Def.FT_PER_SQUARE; ii++){
				ArrayList<Integer> row = new ArrayList<Integer>();
				for(int jj = 0; jj < Def.SENSOR_MAP_SIZE_Y/Def.FT_PER_SQUARE; jj++)
					row.add(Def.UNASSIGNED_CODE);
				grid.add(row);
			}
			grid.get(Def.SENSOR_MAP_QUADCOPTER_POSITION.x).set(Def.SENSOR_MAP_QUADCOPTER_POSITION.y,Def.QUADCOPTER_CODE);
		}
		
		public void addSensors(ArrayList<Sensor> list){
			sensor_list = list;
		}
	/***** END INIT *****/
		
	public void setDirection(double direction){ this.direction = direction; }
	public ArrayList<ArrayList<Integer>> getMap(){ return grid; }
		
	public void performRanging(){
		clear();
		//TODO: need ranging protocol based on actual sensor needs.
		
		//naive ranging method
		for(int ii = 0; ii < sensor_list.size(); ii++){
			sensor_list.get(ii).performRanging();
			double distance = sensor_list.get(ii).distance;
			double angle = sensor_list.get(ii).angle + direction;
			if(distance != -1){				
				Point new_location = Def.convertDistanceToGridPosition(new Point(22,22), distance, angle, Def.FT_PER_SQUARE);
				if(new_location.x > 0 && new_location.y > 0 && new_location.x < grid.size() && new_location.y < grid.get(0).size()){
					fillSquares(new Point(22,22), new_location, distance, angle);
					update(new_location.x, new_location.y, Def.WALL_CODE);
				}
			} else{
				Point end = Def.convertDistanceToGridPosition(new Point(22,22), sensor_list.get(ii).cutoff, angle, Def.FT_PER_SQUARE);
				if(end.x > 0 && end.y > 0 && end.x < grid.size() && end.y < grid.get(0).size()){
					fillSquares(new Point(22,22), end, sensor_list.get(ii).cutoff, angle);
					update(end.x, end.y, Def.MOVABLE_AREA_CODE);
					updateMainMap(end.x, end.y);
				}
			}
		}
		
		update(Def.SENSOR_MAP_QUADCOPTER_POSITION.x, Def.SENSOR_MAP_QUADCOPTER_POSITION.y, Def.QUADCOPTER_CODE);
	}
	
	public void fillSquares(Point start, Point end, double distance, double angle){
		ArrayList<Point> points_seen = new ArrayList<Point>();
		
		double dd = 0;
		while(dd < distance){
			Point new_location = Def.convertDistanceToGridPosition(start, dd, angle, Def.FT_PER_SQUARE);
			if(new_location.equals(end))
				break;
			if(!points_seen.contains(new_location)){
				update(new_location.x, new_location.y, Def.MOVABLE_AREA_CODE);
				updateMainMap(new_location.x, new_location.y);
				points_seen.add(new_location);
			}
			dd++;
		}
		
	}
	
	public int get(int xx, int yy){
		return grid.get(xx).get(yy);
	}
	
	private void clear(){
		for(int ii = 0; ii < grid.size(); ii++){
			for(int jj = 0; jj < grid.get(ii).size(); jj++){
				try{
					update(ii,jj, Def.UNASSIGNED_CODE);
				} catch(Exception e){
					System.out.println("Error");
				}
			}
		}
		update(Def.SENSOR_MAP_QUADCOPTER_POSITION.x, Def.SENSOR_MAP_QUADCOPTER_POSITION.y, Def.QUADCOPTER_CODE);
	}
	
	/***** GUI CODE *****/
		private void update(int xx, int yy, int type){
			grid.get(xx).set(yy, type);
			gui.update_square(xx, yy);
		}
		
		public void attachGUI(full_grid gui){this.gui = gui;}
		
		public void importMap(Map map){ this.map = map; }
		
		private void updateMainMap(int xx, int yy){
			Point t = map.getQuadcopterPosition();
			if(map.get(t.x + xx - Def.SENSOR_MAP_QUADCOPTER_POSITION.x, t.y + yy - Def.SENSOR_MAP_QUADCOPTER_POSITION.y) == Def.MOVABLE_AREA_CODE)
				map.update(t.x + xx - Def.SENSOR_MAP_QUADCOPTER_POSITION.x, t.y + yy - Def.SENSOR_MAP_QUADCOPTER_POSITION.y, Def.SENSED_AREA_CODE);
		}
	/***** END GUI CODE *****/
}
