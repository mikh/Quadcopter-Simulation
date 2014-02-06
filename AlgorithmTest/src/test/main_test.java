package test;

//TODO: comments and control code
//TODO: Streamline

import gui.GUI;
import defaults.Def;
import map.Map;
import quadcopter.Quadcopter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.InputStreamReader;
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

			Def.output(bw, String.format("Started creating gui at %dms\r\n", System.currentTimeMillis() - time_start));
			time_elapsed = System.currentTimeMillis();
			
			
			Def.output(bw, String.format("Finished creating gui at %dms. Time elapsed: %dms\r\n", System.currentTimeMillis() - time_start, System.currentTimeMillis() - time_elapsed));

			
			Def.output(bw, String.format("Started creating map at %dms\r\n", System.currentTimeMillis() - time_start));
			time_elapsed = System.currentTimeMillis();
			Map new_map = new Map(bw);
			GUI gg = new GUI(bw, new_map);
			new_map.attachGUI(gg);
			new_map.generate(Def.SEED);
			//new_map.load_map(Def.SAMPLE_MAP_1);
			new_map.printMap();  //prints map to map.txt
			Def.output(bw, String.format("Finished creating map at %dms. Time elapsed: %dms\r\n", System.currentTimeMillis() - time_start, System.currentTimeMillis() - time_elapsed));
			
			
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
	
}
