package gui;

//TODO: comments and control code
//TODO: Streamline

import defaults.Def;
import map.ProtoMap;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedWriter;
import java.io.IOException;
import javax.swing.JFrame;

public class GUI {
	
	private JFrame frame_map, frame_dynamic, frame_sensor, frame_stats;
	private BufferedWriter log;
	private full_grid grid, grid_sensor, grid_dynamic;
	
	
	@SuppressWarnings("unused")
	public GUI(BufferedWriter log_file, ProtoMap mmap, ProtoMap sensor_map, ProtoMap dynamic_map) throws IOException{
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
		frame_map = new JFrame("Actual Map");

		frame_map.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame_map.setSize(Def.FRAME_SIZE_X, Def.FRAME_SIZE_Y);
		frame_map.setLayout(new GridBagLayout());
		frame_map.setLocation(Def.FRAME_POSITION_X, Def.FRAME_POSITION_Y);
		
		frame_dynamic = new JFrame("Dynamic Map");
		frame_dynamic.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame_dynamic.setSize(Def.FRAME_DYNAMIC_SIZE_X, Def.FRAME_DYNAMIC_SIZE_Y);
		frame_dynamic.setLocation(Def.FRAME_DYNAMIC_POSITION_X, Def.FRAME_DYNAMIC_POSITION_Y);
		
		frame_sensor = new JFrame("Sensor Map");
		frame_sensor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame_sensor.setSize(Def.FRAME_SENSOR_SIZE_X, Def.FRAME_SENSOR_SIZE_Y);
		frame_sensor.setLocation(Def.FRAME_SENSOR_POSITION_X, Def.FRAME_SENSOR_POSITION_Y);
		
		frame_stats = new JFrame("Statistics");
		frame_stats.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame_stats.setSize(Def.FRAME_STATS_SIZE_X, Def.FRAME_STATS_SIZE_Y);
		frame_stats.setLocation(Def.FRAME_STATS_POSITION_X, Def.FRAME_STATS_POSITION_Y);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 5;
		grid = new full_grid(log, mmap, frame_map, Def.MAP_SIZE_FT_X, Def.MAP_SIZE_FT_Y);
		frame_map.add(grid, c);
			
		frame_map.addComponentListener(new ComponentListener(){
			
			@Override
			public void componentResized(ComponentEvent evt){
				grid.resize();
			}

			@Override public void componentHidden(ComponentEvent arg0) {}
			@Override public void componentMoved(ComponentEvent arg0) {}
			@Override public void componentShown(ComponentEvent arg0) {}
		});
		
		grid_sensor = new full_grid(log, sensor_map, frame_sensor, Def.SENSOR_MAP_SIZE_X, Def.SENSOR_MAP_SIZE_Y);
		frame_sensor.add(grid_sensor);
		frame_sensor.addComponentListener(new ComponentListener(){
			@Override
			public void componentResized(ComponentEvent evt){
				grid_sensor.resize();
			}

			@Override public void componentHidden(ComponentEvent arg0) {}
			@Override public void componentMoved(ComponentEvent arg0) {}
			@Override public void componentShown(ComponentEvent arg0) {}
		});

		grid_dynamic = new full_grid(log, dynamic_map, frame_dynamic, Def.DYNAMIC_MAP_INITIAL_SIZE_X*Def.FT_PER_SQUARE, Def.DYNAMIC_MAP_INITIAL_SIZE_Y*Def.FT_PER_SQUARE);
		frame_dynamic.add(grid_dynamic);
		frame_dynamic.addComponentListener(new ComponentListener(){
			@Override
			public void componentResized(ComponentEvent evt){
				grid_dynamic.resize();
			}

			@Override public void componentHidden(ComponentEvent arg0) {}
			@Override public void componentMoved(ComponentEvent arg0) {}
			@Override public void componentShown(ComponentEvent arg0) {}
		});
			
		frame_map.setVisible(true);
		frame_dynamic.setVisible(true);
		frame_sensor.setVisible(true);
		frame_stats.setVisible(true);
	}
	
	public void update_square(int xx, int yy){grid.update_square(xx, yy);}
}