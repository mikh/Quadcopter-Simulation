package gui;

import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import map.ProtoMap;
import defaults.Def;

public class full_grid extends JPanel{
	
	private int sqsi;
	private BufferedWriter log;
	ArrayList<ArrayList<square>> grid;
	private JFrame jf;
	private int sq_row, sq_col;
	private ProtoMap map;
	
	@SuppressWarnings("unused")
	public full_grid(BufferedWriter log_file, ProtoMap mmap, JFrame frame, int ft_x, int ft_y) throws IOException{
		log = log_file;
		jf = frame;
		map = mmap;
		
		log.write("Obtaining number of squares...\t");
		int xx = ft_x, yy = ft_y;
		
		if((Def.MAP_SIZE_FT_X % Def.FT_PER_SQUARE) != 0){
			xx += Def.FT_PER_SQUARE - (Def.MAP_SIZE_FT_X % Def.FT_PER_SQUARE);
			log.write("\r\nMap Foot size is not compatible with foot per square size. Changing Map size to " + xx + ". Please note this may create further problems down the road.\r\n");
		}
		if((Def.MAP_SIZE_FT_Y % Def.FT_PER_SQUARE) != 0){
			yy += Def.FT_PER_SQUARE - (Def.MAP_SIZE_FT_Y % Def.FT_PER_SQUARE);
			log.write("\r\nMap Foot size is not compatible with foot per square size. Changing Map size to " + yy + ". Please note this may create further problems down the road.\r\n");

		}
		
		sq_row = xx/Def.FT_PER_SQUARE;
		sq_col = yy/Def.FT_PER_SQUARE;
		
		log.write(sq_row + "x" + sq_col+"\r\n");
		
		//square size in pixels = total_pixels/sq_row
		// if 900/150
		
		log.write("Obtaining pixels per square...\t");
		xx = Def.MAP_SIZE_PIXELS_X;
		yy = Def.MAP_SIZE_PIXELS_Y;
		
		if(Def.MAP_SIZE_PIXELS_X % sq_row != 0){
			xx = sq_row * ((Def.MAP_SIZE_PIXELS_X / sq_row) + 1);
			log.write("\r\nMap pixel size is not compatible with number of squares. Changing Map size to " + xx + ". Note that this may cause further problems in the graphics.\r\n");
		}
		if(Def.MAP_SIZE_PIXELS_Y % sq_col != 0){
			yy = sq_col * ((Def.MAP_SIZE_PIXELS_Y / sq_col) + 1);
			log.write("\r\nMap pixel size is not compatible with number of squares. Changing Map size to " + yy + ". Note that this may cause further problems in the graphics.\r\n");
		}
		
		sqsi = xx/sq_row;
		
		log.write(sqsi + "x" + sqsi + "\r\n");
		
		setSize(xx, yy);
		setLayout(new GridLayout(sq_row, sq_col));	
		grid = new ArrayList<ArrayList<square>>();
		
		
		for(int ii = 0; ii < sq_row; ii++){
			grid.add(new ArrayList<square>());
			for(int jj = 0; jj < sq_col; jj++){
				square sq = new square(sqsi, mmap, ii, jj);
				add(sq);
				grid.get(ii).add(sq);
			}
		}
		mmap.attachGUI(this);
	}
	
	public void update_square(int xx, int yy){grid.get(xx).get(yy).revalidate();grid.get(xx).get(yy).repaint();}
	
	public void recreate(int ft_x, int ft_y){
		removeAll();
		sq_row = ft_x/Def.FT_PER_SQUARE;
		sq_col = ft_y/Def.FT_PER_SQUARE;
		int xx = jf.getHeight();
		
		int max = Def.max(sq_row, sq_col);
		
		if(xx/max != sqsi)
			sqsi = xx/max;
		setLayout(new GridLayout(sq_row, sq_col));	
		grid = new ArrayList<ArrayList<square>>();
		
		
		for(int ii = 0; ii < sq_row; ii++){
			grid.add(new ArrayList<square>());
			for(int jj = 0; jj < sq_col; jj++){
				square sq = new square(sqsi, map, ii, jj);
				add(sq);
				grid.get(ii).add(sq);
			}
		}
	//	jf.removeAll();
	//	jf.add(this);
		jf.revalidate();
		jf.repaint();
		resize();
	}
	
	public void resize(){
		int xx = jf.getHeight();
		int max = Def.max(sq_row, sq_col);
		if(xx/max != sqsi){
			sqsi = xx/max;
			
			for(int ii = 0; ii < sq_row; ii++){
				for(int jj = 0; jj < sq_col; jj++){
					grid.get(ii).get(jj).updateSize(sqsi);
					grid.get(ii).get(jj).revalidate();
					grid.get(ii).get(jj).repaint();
				}
			}
		}
	}
}