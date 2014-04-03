//package communication;
//import java.io.IOException;
//import java.util.ArrayList;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.Queue;
import java.util.LinkedList;
import java.util.TooManyListenersException;
import java.io.*;
//import gnu.io.*;

public class Pilot{
	private static Pilot myPilot;
	private Pilot() throws TooManyListenersException { 
		uartArduPilot = new UartDriver2("/dev/ttyO4"); 
		uartArduPilot.initialize();
		uartArduPilot.serialPort.addEventListener(new PilotSerialPortEventListener()); // Throws TooManyListenersException
	}
    public static Pilot getInstance() throws TooManyListenersException{
    	if(myPilot == null) myPilot = new Pilot();
    	return myPilot;
    }
    
    private UartDriver2 uartArduPilot;
  private static Queue<String> messageQueue = new LinkedList<String>();
  
  // Variables read from ArduPilot
  private boolean flyMode;
  private double accX;
  private double accY;
  private double accZ;
  private double gyroX;
  private double gyroY;
  private double gyroZ;
  private int battery;
  private int compass;
  
  
  private void sendMessage() throws IOException {
	  uartArduPilot.output.write(String.format("s%02d\n",messageQueue.size()).getBytes()); 
      //System.out.println(String.format("s%02d",messageQueue.size()));

      // TODO check response

       while(messageQueue.size() > 0)
    	   uartArduPilot.output.write(messageQueue.poll().getBytes());
    return;
  }
  
  private boolean receiveMessage(){
    if(uartArduPilot.receivedQueue.isEmpty())
    return false;
    return true;
  }

  public void setPitch(int v){ messageQueue.add(String.format("p%04d\n",v)); }
  public void setYaw(int v){ messageQueue.add(String.format("y%04d\n",v)); }
  public void setRoll(int v){ messageQueue.add(String.format("r%04d\n",v)); }
  public void setThrottle(int v){ messageQueue.add(String.format("t%04d\n",v)); }
  public void setArmed(int v){ messageQueue.add(String.format("a%04d\n",v)); }  
  public void powerOff() throws IOException{
    messageQueue.add("z0001");
    sync();
    return;
  }
 
  public boolean getFyMode() { return flyMode; }
  public double getAccX() { return accX; }
  public double getAccY() { return accY; }
  public double getAccZ() { return accZ; }
  public double getGyroX() { return gyroX; }
  public double getGyroY() { return gyroY; }
  public double getGyroZ() { return gyroZ; }
  public int getBattery() { return battery; }
  public int getCompass() { return compass; }
  
  public void sync() throws IOException {
	  sendMessage();
	  receiveMessage();
    return;
  }
  
  private class PilotSerialPortEventListener implements SerialPortEventListener{
	  private BufferedReader input;
	  PilotSerialPortEventListener() {
		   input = uartArduPilot.input;
	  }
	  public synchronized void serialEvent(SerialPortEvent oEvent) {
		  if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
	    
			  try {
				  String str = input.readLine();
				  // parse
	    
			  } catch (Exception e) {
				  System.err.println(e.toString()); // TODO
			  }
		  }
	  // Ignore all the other eventTypes, but you should consider the other ones.
	 }
  }
  
  // CODE FOR UART COMMUNICATION
 //void connect( String portName ) throws Exception {
    /*System.out.println("Opening connection to " + portName);
    System.out.println(CommPortIdentifier.getPortIdentifiers());
    System.out.println("\n\n");
     CommPortIdentifier.addPortName(portName, CommPortIdentifier.PORT_SERIAL, null);
     System.out.println("Added port name");
    CommPortIdentifier portIdentifier = CommPortIdentifier
        .getPortIdentifier( portName );
         System.out.println("Port Identifier created " + portIdentifier.getName() + " " + portIdentifier.toString());
    if(portIdentifier == null) System.out.println("portIdentifier == null");
    if( portIdentifier.isCurrentlyOwned() ) {
      System.out.println( "Error: Port is currently in use" );
    } else {
      int timeout = 2000;
       System.out.println("commPort creating ");
      SerialPort commPort = (SerialPort) portIdentifier.open( "", timeout);
      System.out.println("commPort created");
 
      if( commPort instanceof SerialPort ) {
        SerialPort serialPort = commPort;
        System.out.println("serialPort created");
        serialPort.setSerialPortParams( 9600,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_NONE );
        System.out.println("serialPort params defined");
        InputStream in = serialPort.getInputStream();
        System.out.println("Input stream created");
        OutputStream out = serialPort.getOutputStream();
        System.out.println("output stream created");
 
        ( new Thread( new SerialReader( in ) ) ).start();
        System.out.println("input thread started");
        ( new Thread( new SerialWriter( out ) ) ).start();
         System.out.println("output thread started");
 
      } else {
        System.out.println( "Error: Only serial ports are handled by this example." );
      }
    }
     System.out.println("Done with connect");*/
//  }
  } 
  
  /*
  public static void main (String[] args) {
      int RUN_TIME = 30000;
      int DELAY_MS = 333;
      int HOVER_HEIGHT = 45;
      int TAKEOFF_MAX_THROTTLE = 400;
      int MIN_THROTTLE = 150;
      int THROTTLE_INCREMENT = 15;
      Pilot pilot = new Pilot();
      try {
          pilot.connect("/dev/ttyO4" );
      } catch (Exception ex) {
          System.out.println(ex.getLocalizedMessage());
          System.out.println(ex.toString());
          ex.printStackTrace();
          System.out.println("Could not open tty, exiting");
          return;
      }  
      pilot.setArmed(1);
      pilot.sync();
       
      try {
          Thread.sleep(1000);
      } catch(InterruptedException ex) {
          System.out.println("Error 1");
          Thread.currentThread().interrupt();
      }
     
      //Takeoff
      int ttr = MIN_THROTTLE;
      while(ttr < TAKEOFF_MAX_THROTTLE){
          pilot.setThrottle(ttr);
          System.out.println("TAKEOFF: " + ttr);
          ttr += THROTTLE_INCREMENT;
          pilot.sync();
          try{Thread.sleep(DELAY_MS);} catch(Exception e){}
      }
    
      SonarAnalogSensorInterface sa = new SonarAnalogSensorInterface("/sys/devices/ocp.2/helper.14/AIN1");
      pilot.setDesiredAlt(HOVER_HEIGHT);
      int loop = 0;
      int max_loop = RUN_TIME/DELAY_MS;
      while(loop < max_loop){
          double currentHeight = sa.getRanging();
          pilot.setThrottleWithAltitude((int)currentHeight);
          pilot.sync();
          try{Thread.sleep(DELAY_MS);} catch(Exception e){}
          loop++;
      }
      
    
      //land
      ttr = pilot.getThrottle();
      while(ttr > MIN_THROTTLE){
          pilot.setThrottle(ttr);
          System.out.println("LAND: " + ttr);
          ttr-= THROTTLE_INCREMENT;
          pilot.sync();
          try{Thread.sleep(333);} catch(Exception e){}
      }
      System.out.println("Done.");
      return;
    
       // Takeoff
      /*
       for(int ii = 0; ii < 14; ii++){
      pilot.setThrottle(ii * 25 + 150);
      pilot.sync();
      try {
       Thread.sleep(1000);
      } catch(InterruptedException ex) {
          System.out.println("Error 2");
       Thread.currentThread().interrupt();
      }
       //pilot.setYaw(300);
       }
       
       // Hover
      try {
       Thread.sleep(5000);
      } catch(InterruptedException ex) {
          System.out.println("Error 3");
       Thread.currentThread().interrupt();
      }
    
       for(int ii = 0; ii < 14; ii++){
      pilot.setThrottle(500 - ii * 25);
      pilot.sync();
      try {
       Thread.sleep(1000);
      } catch(InterruptedException ex) {
          System.out.println("Error 4");
       Thread.currentThread().interrupt();
      }
       }*/
    //}  
  //pilot.setYaw(300);
  //pilot.powerOff(); 
//} 



/* import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 
public class TwoWaySerialComm {
 
  void connect( String portName ) throws Exception {
    CommPortIdentifier portIdentifier = CommPortIdentifier
        .getPortIdentifier( portName );
    if( portIdentifier.isCurrentlyOwned() ) {
      System.out.println( "Error: Port is currently in use" );
    } else {
      int timeout = 2000;
      CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );
 
      if( commPort instanceof SerialPort ) {
        SerialPort serialPort = ( SerialPort )commPort;
        serialPort.setSerialPortParams( 57600,
                                        SerialPort.DATABITS_8,
                                        SerialPort.STOPBITS_1,
                                        SerialPort.PARITY_NONE );
 
        InputStream in = serialPort.getInputStream();
        OutputStream out = serialPort.getOutputStream();
 
        ( new Thread( new SerialReader( in ) ) ).start();
        ( new Thread( new SerialWriter( out ) ) ).start();
 
      } else {
        System.out.println( "Error: Only serial ports are handled by this example." );
      }
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
        while( ( len = this.in.read( buffer ) ) &gt; -1 ) {
          System.out.print( new String( buffer, 0, len ) );
        }
      } catch( IOException e ) {
        e.printStackTrace();
      }
    }
  }
 
  public static class SerialWriter implements Runnable {
 
    OutputStream out;
 
    public SerialWriter( OutputStream out ) {
      this.out = out;
    }
 
    public void run() {
      try {
        int c = 0;
        while( ( c = System.in.read() ) &gt; -1 ) {
          this.out.write( c );
        }
      } catch( IOException e ) {
        e.printStackTrace();
      }
    }
  }
 
  public static void main( String[] args ) {
    try {
      ( new TwoWaySerialComm() ).connect( "/dev/ttyUSB0" );
    } catch( Exception e ) {
      e.printStackTrace();
    }
  }
} */



class SonarAnalogSensorInterface{
  String port;
  public SonarAnalogSensorInterface(String port){
    this.port = port;
    System.out.println("Sensor Active.");
  }

  public double getRanging(){
    double rr = -1, rr1, rr2, rr3;
    try{
      BufferedReader br = new BufferedReader(new FileReader(port));
      String range = br.readLine();
      br.close();
      rr1 = Double.parseDouble(range);
      rr1 /= 3.2;
      try{Thread.sleep(30);} catch(Exception e){}
      br = new BufferedReader(new FileReader(port));
      range = br.readLine();
      br.close();
      rr2 = Double.parseDouble(range);
      rr2 /= 3.2;
      try{Thread.sleep(30);} catch(Exception e){}
      br = new BufferedReader(new FileReader(port));
      range = br.readLine();
      br.close();
      rr3 = Double.parseDouble(range);
      rr3 /= 3.2;
      rr = (rr1 + rr2 + rr3)/3.0;
    } catch(IOException e){
      System.out.println("Error. IOException with reading port");
    }
    System.out.println("Range = " + rr);
    return rr;
  }
}