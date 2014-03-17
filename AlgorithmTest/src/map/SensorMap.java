package map;

import java.util.ArrayList;
import defaults.Def;
import gui.full_grid;

public class SensorMap extends ProtoMap{
	private ArrayList<ArrayList<Integer>> grid;
	private full_grid gui;
	
	/***** INIT ****/
		public SensorMap(){
			grid = new ArrayList<ArrayList<Integer>>();
			for(int ii = 0; ii < Def.SENSOR_MAP_SIZE_X; ii++){
				ArrayList<Integer> row = new ArrayList<Integer>();
				for(int jj = 0; jj < Def.SENSOR_MAP_SIZE_Y; jj++)
					row.add(Def.UNASSIGNED_CODE);
				grid.add(row);
			}
			grid.get(Def.SENSOR_MAP_QUADCOPTER_POSITION.x).set(Def.SENSOR_MAP_QUADCOPTER_POSITION.y,Def.QUADCOPTER_CODE);
		}
	/***** END INIT *****/
	
	public int get(int xx, int yy){
		return grid.get(xx).get(yy);
	}
	
	private void clear(){
		for(int ii = 0; ii < Def.SENSOR_MAP_SIZE_X; ii++){
			for(int jj = 0; jj < Def.SENSOR_MAP_SIZE_Y; jj++)
				update(ii,jj, Def.UNASSIGNED_CODE);
		}
		update(Def.SENSOR_MAP_QUADCOPTER_POSITION.x, Def.SENSOR_MAP_QUADCOPTER_POSITION.y, Def.QUADCOPTER_CODE);
	}
	
	/***** GUI CODE *****/
		private void update(int xx, int yy, int type){
			grid.get(xx).set(yy, type);
			gui.update_square(xx, yy);
		}
		
		public void attachGUI(full_grid gui){this.gui = gui;}
	/***** END GUI CODE *****/
}
