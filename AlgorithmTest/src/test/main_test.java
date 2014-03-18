package test;

//TODO: comments and control code
//TODO: Streamline

import gui.GUI;
import defaults.Def;
import map.Map;
import map.SensorMap;
import quadcopter.Quadcopter;
import sensor.Sensor;
import sensor.SensorSimulator;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;


public class main_test {
	
	public static void main(String []args){
		
		try{
			//Setup log file
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(Def.LOG_FILE_PATH));
			
			Def.output(bw, String.format("Starting Test at %s\r\n\r\n", (new Date()).toString()));
			long time_start = System.currentTimeMillis(), time_elapsed;
			
			/*** Test Initialization ***/
			Def.output(bw, "Test Initialization Phase\r\n");
			
			Def.output(bw, String.format("Started creating map and GUI at %dms\r\n", System.currentTimeMillis() - time_start));
			time_elapsed = System.currentTimeMillis();
			
			Def.output(bw, "Declaring Map...\r\n");
			Map new_map = new Map(bw);
			Def.output(bw, "Declaring Sensor Map...\r\n");
			SensorMap sensor_map = new SensorMap();
			Def.output(bw, "Creating GUI...\r\n");
			GUI gg = new GUI(bw, new_map, sensor_map);
			
			//new_map.generate(Def.SEED);
			//new_map.printMap("sample_map_2.txt");
			Def.output(bw, "Loading map...\r\n");
			new_map.load_map(Def.SAMPLE_MAP_2);
			
			Def.output(bw, "Creating sensors...\r\n");
			ArrayList<Sensor> sensor_list = createSensors(1);
			ArrayList<SensorSimulator> sensor_simulator_list = createSimulationSensors(sensor_list, new_map);
			sensor_map.addSensors(sensor_list);
			sensor_map.importMap(new_map);
			new_map.setQuadcopterPositionAndDirection(new Point(42, 42), 90);
			sensor_map.setDirection(90);
			sensor_map.performRanging();
			Def.output(bw, String.format("Finished creating map and GUI at %dms. Time elapsed: %dms\r\n", System.currentTimeMillis() - time_start, System.currentTimeMillis() - time_elapsed));
			
			
			
			/*
			Def.output(bw, String.format("Started creating copter at %dms\r\n", System.currentTimeMillis() - time_start));
			time_elapsed = System.currentTimeMillis();
			Quadcopter copter = new Quadcopter(bw, new_map, gg);
			copter.sense();
			Def.output(bw, String.format("Finished creating copter at %dms. Time elapsed: %dms\r\n", System.currentTimeMillis() - time_start, System.currentTimeMillis() - time_elapsed));
			Def.output(bw, "Test Initialization complete\r\n\r\n");
			
			Def.output(bw, "Testing Phase\r\n");
			Def.output(bw, String.format("Starting testing phase at %dms.\r\n", System.currentTimeMillis() - time_start));
			time_elapsed = System.currentTimeMillis();
			
			
			int ii = 0;
			while(ii < 60000){
				
				copter.movement_protocol(false);
				copter.move();
				copter.sense();
				//copter.printPath();
				Thread.sleep(100);
				ii++;
				
				//BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in)); 
				//stdin.readLine();
			}
			
			Def.output(bw, String.format("Testing Phase complete at %dms. Took %dms.\r\n", System.currentTimeMillis() - time_start, System.currentTimeMillis() - time_elapsed));
			*/
			bw.close();
		} catch(IOException /*| InterruptedException*/ e){
			System.out.println("An exception occured within the code.\r\nYou may now panic\r\n");
			System.exit(-1);
		}		
		
		System.out.println("Application Exit");
	}
	
	public static ArrayList<Sensor> createSensors(int arrangement){
		ArrayList<Sensor> list = new ArrayList<Sensor>();
		if(arrangement == 1){
			/** assuming 8 UV sensors , 2 on each side about 6 inches from the center on each side and one laser rangefinder in the front. **/
			int id = 0;
			//UV sensors
			for(int ii = 0; ii < 360; ii += 90){
				for(int jj = -5; jj < 6; jj += 10){
					Sensor ss = new Sensor(id++, "sonarSR04");
					ss.angle = ii;
					ss.position = jj;
					ss.cutoff = Def.SONAR_SENSOR_CUTOFF;
					ss.max_distance = Def.SONAR_SENSOR_MAX_DISTANCE;
					ss.min_distance = Def.SONAR_SENSOR_MIN_DISTANCE;
					ss.rangingDelay = Def.SONAR_SENSOR_RANGING_DELAY;
					list.add(ss);
				}
			}
			
			//laser rangefinder
			Sensor ls = new Sensor(id, "laser");
			ls.angle = 0;
			ls.position = 0;
			ls.cutoff = Def.LASER_SENSOR_CUTOFF;
			ls.max_distance = Def.LASER_SENSOR_MAX_DISTANCE;
			ls.min_distance = Def.LASER_SENSOR_MIN_DISTANCE;
			ls.rangingDelay = Def.LASER_SENSOR_RANGING_DELAY;
			list.add(ls);
		}
		return list;
	}
	
	public static ArrayList<SensorSimulator> createSimulationSensors(ArrayList<Sensor> list, Map map){
		ArrayList<SensorSimulator> sen_list = new ArrayList<SensorSimulator>();
		for(int ii = 0; ii < list.size(); ii++){
			sen_list.add(list.get(ii).fake_sensor());
			sen_list.get(ii).importMap(map);
		}
		return sen_list;
	}
}
