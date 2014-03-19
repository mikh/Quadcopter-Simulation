package sensor;

import java.awt.Point;
import java.util.ArrayList;

import defaults.Def;
import map.Map;

public class SensorSimulator {
	public int ID;
	public String type;
	public double angle;
	public double position;
	public double max_range;
	
	Map map;
	
	public SensorSimulator(){}
	
	public double getRange(){
		Point q_pos = map.getQuadcopterPosition();
		double q_ang = map.getQuadcopterDirection();
		
		double aa = angle + q_ang;
		if(aa > 360) aa -= 360;
		
		double dd = 0;
		
		ArrayList<Point> list_positions = new ArrayList<Point>();
		list_positions.add(q_pos);
		while(dd < max_range){
			Point n_pos = Def.convertDistanceToGridPosition(q_pos, dd, aa, Def.FT_PER_SQUARE);
			Point size = map.getSizeOfGrid();
			if(n_pos.x < size.x && n_pos.y < size.y && n_pos.x > 0 && n_pos.y > 0 && !list_positions.contains(n_pos)){
				int type = map.get(n_pos.x, n_pos.y);
				if(type == Def.WALL_CODE)
					break;
				list_positions.add(n_pos);
			}
			dd++;
		}
		if(dd > max_range) return max_range;
		else return dd;
	}
	
	public void importMap(Map map){ this.map = map; }
}
