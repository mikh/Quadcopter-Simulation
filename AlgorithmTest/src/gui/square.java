package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JComponent;

import map.ProtoMap;
import defaults.Def;

public class square extends JComponent{
	private int sqsi, xx, yy;
	private ProtoMap ss;
	private ArrayList<ArrayList<Integer>> inter;
	private int type;
	
	public square(int square_size, ProtoMap mmap, int x_coord, int y_coord){
		ss = mmap;
		sqsi = square_size;
		xx = x_coord;
		yy = y_coord;
		setBounds(0, 0, sqsi, sqsi);
		type = 0;
	}
	
	public square(int square_size, ArrayList<ArrayList<Integer>> internal, int x_coord, int y_coord){
		inter = internal;
		sqsi = square_size;
		xx = x_coord;
		yy = y_coord;
		setBounds(0, 0, sqsi, sqsi);
		type = 1;
	}
	
	public void updateSize(int new_square){ sqsi = new_square; setBounds(0, 0, sqsi, sqsi);}

	@Override public void paintComponent(Graphics g){
		super.paintComponent(g);
		int val = -1;
		if(type == 0) val = ss.get(xx, yy);
		else if(type == 1) val = inter.get(xx).get(yy);
		if(val == -1)
			g.setColor(Color.WHITE);
		else{
			switch(val){
				case 0:
					g.setColor(Color.WHITE);
					break;
				case 1:
					g.setColor(Def.WALL_COLOR);
					break;
				case 2:
					g.setColor(Def.MOVABLE_AREA_COLOR);
					break;
				case 3:
					g.setColor(Def.PERSON_COLOR);
					break;
				case 4:
					g.setColor(Def.QUADCOPTER_LOCATION_COLOR);
					break;
				case 5:
					g.setColor(Def.SENSED_AREA_COLOR);
					break;
				case 6:
					g.setColor(Def.SENSED_AND_SEARCHED_AREA_COLOR);
					break;
				case 7:
					g.setColor(Def.CURRENTLY_SENSING_AREA_COLOR);
					break;
				case 8:
					g.setColor(Def.CURRENTLY_SEARCHING_AREA_COLOR);
					break;
				case 9:
					g.setColor(Def.SENSED_AND_SEARCHED_AREA_COLOR);
					break;
				case 10:
					g.setColor(Def.PATH_COLOR);
					break;
				case 11:
					g.setColor(Def.CUSTOM_COLOR);
					break;
				default:
					g.setColor(Color.WHITE);
					break;
			}
		}
		g.fillRect(0, 0, sqsi, sqsi);
		if(val == 9){
			g.setColor(Def.PATH_COLOR);
			g.fillRect(sqsi/4, sqsi/4, sqsi/2, sqsi/2);
		}
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, sqsi-1, sqsi-1);
	}
	
	@Override public Dimension getMinimumSize(){ return new Dimension(sqsi, sqsi);}
	@Override public Dimension getPreferredSize(){ return new Dimension(sqsi, sqsi);}
}