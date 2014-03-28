//import java.io.IOException;
//import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.*;
import javax.comm.CommPortIdentifier;

public class Pilot{
  public Pilot() { }
  
  private static Queue<String> messageQueue = new LinkedList<String>();
  private boolean flyMode;
  private int accX;
  private int accY;
  private int accZ;
  private int battery;
  private int compass;
  
  private static boolean sendingMessage;
  
  private boolean sendMessage() {
    System.out.println("Messsage to be sent:");
    String str = (String.format("s%02d",messageQueue.size()));
    System.out.println(str);
    for(int ii = 0; ii <= messageQueue.size(); ii++)
      System.out.println(messageQueue.poll());
    sendingMessage = true;
    while(sendingMessage) {System.out.println("SENDING MESSAGE"); }
    System.out.println("DONE SENDING");
    return true;
  }
  
  private boolean receiveMessage(){
    
    return false;
  }
  
  public void setPitch(int v){ messageQueue.add(String.format("p%04d",v)); }
  public void setYaw(int v){ messageQueue.add(String.format("y%04d",v)); }
  public void setRoll(int v){ messageQueue.add(String.format("r%04d",v)); }
  public void setThrottle(int v){ messageQueue.add(String.format("t%04d",v)); }
  public void setArmed(int v){ messageQueue.add(String.format("a%04d",v)); }
  
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
    CommPortIdentifier portIdentifier = CommPortIdentifier
        .getPortIdentifier( portName );
    if( portIdentifier.isCurrentlyOwned() ) {
      System.out.println( "Error: Port is currently in use" );
    } else {
      int timeout = 2000;
      CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );
 
      if( commPort instanceof SerialPort ) {
        SerialPort serialPort = ( SerialPort )commPort;
        serialPort.setSerialPortParams( 9600,
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
        while( ( len = this.in.read( buffer ) ) > -1 ) {
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
        while(!sendingMessage) {
         String str = (String.format("s%02d",messageQueue.size()));
         for(int ii = 0; ii <= messageQueue.size(); ii++)
           str += messageQueue.poll();

          this.out.write(str.getBytes());
        }
        sendingMessage = false;
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
  pilot.connect("/dev/ttyUSB0" );
  } catch (Exception ex) {
    System.out.println("Could not open tty, exiting");
    return;
  }
  pilot.setArmed(1);
  pilot.sync();
  
  try {
    Thread.sleep(3000);
} catch(InterruptedException ex) {
    Thread.currentThread().interrupt();
}
  pilot.setThrottle(400);
  pilot.setYaw(300);
  pilot.sync();
 
  try {
    Thread.sleep(10000);
} catch(InterruptedException ex) {
    Thread.currentThread().interrupt();
}
  pilot.powerOff();
 } 
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