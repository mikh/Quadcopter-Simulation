import java.util.Queue;
import java.util.LinkedList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.*;
import gnu.io.*;


public class Laser_test{
	public static void main(String[] args){
		LaserSensorInterface lsi = new LaserSensorInterface("/dev/ttyO4");
		while(true){
			lsi.getRanging();
			try{ Thread.sleep(500); } catch(Exception e){}
		}
	}
}

class LaserSensorInterface{
	String serialPortName;
	InputStream in;
	OutputStream out;
	public LaserSensorInterface(String serialPortName){
		this.serialPortName = serialPortName;
		init();
		System.out.println("Sensor Ready.");
	}

	public void init(){
		try{
	 		System.out.println("Opening connection to " + serialPortName);
	 		System.out.print("adding port name and identifier...  ");
	     	CommPortIdentifier.addPortName(serialPortName, CommPortIdentifier.PORT_SERIAL, null);
	    	CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier( serialPortName );
	    	if(portIdentifier == null) System.out.println("portIdentifier == null");
	   		if( portIdentifier.isCurrentlyOwned() ) {System.out.println( "Error: Port is currently in use" ); }
	   		else {
	   			System.out.println("done.");
	        	int timeout = 2000;
	       		System.out.print("creating comport... ");
	      		SerialPort commPort = (SerialPort) portIdentifier.open( "", timeout);
	       		System.out.println("done.");
	       		if( commPort instanceof SerialPort ) {
	       			System.out.print("creating serial port...  ");
	        		SerialPort serialPort = commPort;
	        		System.out.println("done.");
	        		System.out.print("setting up serial port parameters...  ");
	        		serialPort.setSerialPortParams( 9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
	        		System.out.println("done.");
	        		System.out.print("creating streams...  ");
	       			in = serialPort.getInputStream();
	        		out = serialPort.getOutputStream();
	        		System.out.println("done.");
	        		System.out.print("Starting thread...  ");
	 				(new Thread( new SerialReader( in ) ) ).start();
	 				System.out.println("done.");
	       		} else {
	        		System.out.println( "Error: Only serial ports are handled by this example." );
	      		}
	    	}
	     	System.out.println("Connection complete.");
     	} catch(Exception e){
     		System.out.println("Error could not connect.");
     	}
  	}
	

	public static class SerialReader implements Runnable { 
    	InputStream in;
 
    	public SerialReader( InputStream in ) {
    	    this.in = in;
    	}
 
    	public void run() {
    	    byte[] buffer = new byte[ 1024 ];
    	    int len = -1;
    	    try {
    	    	while( ( len = this.in.read( buffer ) ) > -1 ) {
    	    		System.out.print( new String( buffer, 0, len ) );
    	    	}
    	  	} catch( IOException e ) {
    	    	e.printStackTrace();
    	    }
    	}
    }

	public void getRanging(){	
		try{
			out.write("D".getBytes());
		} catch(Exception e){
			System.out.println("Error Exception occured.");
			e.printStackTrace();
		}
	}

	private void delay(long milliseconds){
		long start = System.currentTimeMillis();
		while(System.currentTimeMillis() - start < milliseconds);
	}
}

	/*
			delay(50);
			boolean pass = false;		
			while(!pass){
				try{
					BufferedReader br = new BufferedReader(new FileReader(rangeFile));
					String line = br.readLine();
					range = Double.parseDouble(line);
					br.close();
					pass = true;
				} catch(IOException e){
					System.out.println("Error opening file for laser sensor range interface.");
				} catch(NullPointerException n){

				}
			}
			*/