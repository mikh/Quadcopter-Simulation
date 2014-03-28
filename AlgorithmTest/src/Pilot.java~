//import java.io.IOException;
//import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;


public class Pilot{
  public Pilot() { }
  
  private static Queue<String> messageQueue = new LinkedList<String>();
  private boolean flyMode;
  private int accX;
  private int accY;
  private int accZ;
  private int battery;
  private int compass;
  
  private boolean sendMessage() {
    String str = (String.format("s%02d",messageQueue.size()));
    //Send to UART
    System.out.println(str);
    for(int ii = 0; ii <= messageQueue.size(); ii++)
      System.out.println(messageQueue.poll());
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
  
 public static void main (String[] args) {
  Pilot pilot = new Pilot();
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