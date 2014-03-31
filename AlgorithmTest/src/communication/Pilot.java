package communication;
//import java.io.IOException;
//import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.*;
import gnu.io.*;

public class Pilot{
  public Pilot() { }
  
  private static Queue<String> messageQueue = new LinkedList<String>();
  private boolean flyMode;
  private int accX;
  private int accY;
  private int accZ;
  private int battery;
  private int compass;
  
  private volatile static boolean sendingMessage = false;
  
  private boolean sendMessage() {
  /*  System.out.println("Messsage to be sent:");
    String str = (String.format("s%02d",messageQueue.size()));
    System.out.println(str);
    for(int ii = 0; ii <= messageQueue.size(); ii++)
      System.out.println(messageQueue.poll());*/
    sendingMessage = true;
   // while(sendingMessage) {System.out.println("SENDING MESSAGE"); }
  //  System.out.println("DONE SENDING");
    return true;
  }
  
  private boolean receiveMessage(){
    
    return false;
  }
  
  public void setPitch(int v){ messageQueue.add(String.format("p%04d\n",v)); }
  public void setYaw(int v){ messageQueue.add(String.format("y%04d\n",v)); }
  public void setRoll(int v){ messageQueue.add(String.format("r%04d\n",v)); }
  public void setThrottle(int v){ messageQueue.add(String.format("t%04d\n",v)); }
  public void setArmed(int v){ messageQueue.add(String.format("a%04d\n",v)); }
  
  public boolean powerOff(){
    messageQueue.add("z0001");
    return sync();
  }
  
  public boolean getFyMode() { return flyMode; }
  public int getAccX() { return accX; }
  public int getAccY() { return accY; }
  public int getAccZ() { return accZ; }
  public int getBattery() { return battery; }
  public int getCompass() { return compass; }
  
  public boolean sync() {
    boolean success = true;
    if(success) success = sendMessage();
    if(success) success = receiveMessage();
    return success;
  }
  
  
  // CODE FOR UART COMMUNICATION
 void connect( String portName ) throws Exception {
    System.out.println("Opening connection to " + portName);
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
     System.out.println("Done with connect");
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
        }          System.out.print( new String( buffer, 0, len ) );

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
      while(true){
       // System.out.println("THREAD");
       // this.out.write(String.format("THREAD").getBytes());
        if(sendingMessage) {
         //String str = (String.format("s%02d",messageQueue.size()));
         this.out.write(String.format("s%02d\n",messageQueue.size()).getBytes()); 
         System.out.println(String.format("s%02d",messageQueue.size()));
         //System.out.println()
         // TODO check response
         //for(int ii = 0; ii < messageQueue.size(); ii++)
          while(messageQueue.size() > 0)
           //str += messageQueue.poll();
           this.out.write(messageQueue.poll().getBytes());
           // System.out.println(messageQueue.poll());
          //this.out.write(str.getBytes());
          sendingMessage = false;
        }
      }
        
/*        int c = 0;
        while( ( c = System.in.read() ) &gt; -1 ) {
          this.out.write( c );
        } */
      } catch( IOException e ) {
        e.printStackTrace();
      }
    }
  } 
  
  
	 public static void main (String[] args) {
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
	  
	  // Takeoff
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
	  }
	 }  
  //pilot.setYaw(300);
  //pilot.powerOff(); 
}



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