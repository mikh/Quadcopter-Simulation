package gui;

//TODO: comments and control code
//TODO: Streamline

import defaults.Def;
import map.Map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI {
	
	private JFrame frame;
	private BufferedWriter log;
	private full_grid grid;
	private internal_grid i_grid;
	
	
	public GUI(BufferedWriter log_file, Map mmap) throws IOException{
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");	//prevent a exception due to a JDK bug
		
		/* Crash Exception Testing */
		if(Def.HALLWAY_LENGTH_FT % Def.FT_PER_SQUARE != 0){
			System.out.println("Hallway Length is not compatible with ft per square. Application Crash.");
			log.write("Hallway Length is not compatible with ft per square. Application Crash.\r\n");
			System.exit(-1);
		}
		if(Def.MINIMUM_SIZE_OF_ROOM_FT % Def.FT_PER_SQUARE != 0){
			System.out.println("Minimum room size is not compatible with ft per square. Application Crash.");
			log.write("Minimum room size is not compatible with ft per square. Application Crash.\r\n");
			System.exit(-1);
		}
		if(Def.MAXIMUM_SIZE_OF_ROOM_FT % Def.FT_PER_SQUARE != 0){
			System.out.println("Maximum room size is not compatible with ft per square. Application Crash.");
			log.write("MAXIMUM room size is not compatible with ft per square. Application Crash.\r\n");
			System.exit(-1);
		}
		if(Def.DOOR_SIZE_FT % Def.FT_PER_SQUARE != 0){
			System.out.println("Door size is not compatible with ft per square. Application Crash.");
			log.write("Door size is not compatible with ft per square. Application Crash.\r\n");
			System.exit(-1);
		}
		
		
		log = log_file;
		

		
		log.write("Creating Frame.\r\n");
		frame = new JFrame("Algorithm Testing");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(Def.FRAME_SIZE_X, Def.FRAME_SIZE_Y);
		frame.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 5;
		grid = new full_grid(log, mmap);
		frame.add(grid, c);
		

		frame.setVisible(true);
	}

	public void addInternalMap(ArrayList<ArrayList<Integer>> internal_map){
		//frame.getContentPane().removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 3;
		i_grid = new internal_grid(log, internal_map);
		frame.add(i_grid, c);
		frame.getContentPane().revalidate();
	}
	
	public void update_square(int xx, int yy){grid.update_square(xx, yy);}
	public void update_i_square(int xx, int yy){i_grid.update_square(xx, yy);}

	
}



class full_grid extends JPanel{
	
	private int sqsi;
	private BufferedWriter log;
	ArrayList<ArrayList<square>> grid;
	
	@SuppressWarnings("unused")
	public full_grid(BufferedWriter log_file, Map mmap) throws IOException{
		log = log_file;
		
		log.write("Obtaining number of squares...\t");
		int xx = Def.MAP_SIZE_FT_X, yy = Def.MAP_SIZE_FT_Y;
		
		if((Def.MAP_SIZE_FT_X % Def.FT_PER_SQUARE) != 0){
			xx += Def.FT_PER_SQUARE - (Def.MAP_SIZE_FT_X % Def.FT_PER_SQUARE);
			log.write("\r\nMap Foot size is not compatible with foot per square size. Changing Map size to " + xx + ". Please note this may create further problems down the road.\r\n");
		}
		if((Def.MAP_SIZE_FT_Y % Def.FT_PER_SQUARE) != 0){
			yy += Def.FT_PER_SQUARE - (Def.MAP_SIZE_FT_Y % Def.FT_PER_SQUARE);
			log.write("\r\nMap Foot size is not compatible with foot per square size. Changing Map size to " + yy + ". Please note this may create further problems down the road.\r\n");

		}
		
		int sq_row = xx/Def.FT_PER_SQUARE, sq_col = yy/Def.FT_PER_SQUARE;
		
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
		
	}
	
	public void update_square(int xx, int yy){grid.get(xx).get(yy).revalidate();grid.get(xx).get(yy).repaint();}
}

class internal_grid extends JPanel{
	private int sqsi;
	private BufferedWriter log;
	ArrayList<ArrayList<square>> grid;
	ArrayList<ArrayList<Integer>> map;
	
	public internal_grid(BufferedWriter log_file, ArrayList<ArrayList<Integer>> internal_map){
		log = log_file;
		map = internal_map;
		
		int grid_x = 2*(Def.SENSOR_RANGE_FT/Def.FT_PER_SQUARE) + 1, grid_y = grid_x;
		
		setSize(Def.INTERNAL_VIEW_X_PX, Def.INTERNAL_VIEW_Y_PX);
		setLayout(new GridLayout(grid_x, grid_y));
		grid = new ArrayList<ArrayList<square>>();
		sqsi = Def.INTERNAL_VIEW_X_PX/grid_x;
		
		System.out.println("Adding internal map");
		
		for(int ii = 0; ii < grid_x; ii++){
			grid.add(new ArrayList<square>());
			for(int jj = 0; jj < grid_y; jj++){
				square sq = new square(sqsi, map, ii, jj);
				add(sq);
				grid.get(ii).add(sq);
			}
		}
	}
	
	public void update_square(int xx, int yy){grid.get(xx).get(yy).revalidate();grid.get(xx).get(yy).repaint();}
}

class square extends JComponent{
	private int sqsi, xx, yy;
	private Map ss;
	private ArrayList<ArrayList<Integer>> inter;
	private int type;
	
	public square(int square_size, Map mmap, int x_coord, int y_coord){
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