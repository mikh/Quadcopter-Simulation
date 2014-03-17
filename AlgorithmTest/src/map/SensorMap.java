package map;

import java.util.ArrayList;
import defaults.Def;

public class SensorMap extends ProtoMap{
	private ArrayList<ArrayList<Integer>> grid;
	
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
	
	public int get(int xx, int yy){
		return grid.get(xx).get(yy);
	}
	
	private void clear(){
		for(int ii = 0; ii < Def.SENSOR_MAP_SIZE_X; ii++){
			for(int jj = 0; jj < Def.SENSOR_MAP_SIZE_Y; jj++)
				grid.get(ii).set(jj, Def.UNASSIGNED_CODE);
		}
		grid.get(Def.SENSOR_MAP_QUADCOPTER_POSITION.x).set(Def.SENSOR_MAP_QUADCOPTER_POSITION.y,Def.QUADCOPTER_CODE);
	}
}
