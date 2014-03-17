package map;

import gui.full_grid;

public abstract class ProtoMap {
	public abstract int get(int xx, int yy); 
	public abstract void attachGUI(full_grid gui);
}
