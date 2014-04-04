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
	  //receiveMessage();
    return;
  }
  
  public void parseCommand(String str){
	  String delims = ",";
	  String messageTokens[] = str.split(delims);
	  //Send to xbee
	  
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
				  parseCommand(str);
				  System.out.println(str);
			  } catch (Exception e) {
				  System.err.println(e.toString()); // TODO
			  }
		  }
	  // Ignore all the other eventTypes, but you should consider the other ones.
	 }
  }

  } 




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