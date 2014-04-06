
//import java.io.IOException;
//import java.util.ArrayList;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

//import java.util.Queue;
//import java.util.LinkedList;

import java.util.TooManyListenersException;
import java.io.*;

public class XbeeInterface{

    private UartDriver2 uartXbee;
 
 private static XbeeInterface myXbee;
 private XbeeInterface() throws TooManyListenersException { 
  uartXbee = new UartDriver2("/dev/ttyO5"); 
  uartXbee.initialize();
  uartXbee.serialPort.addEventListener(new xBeeSerialPortEventListener()); // Throws TooManyListenersException. This is a nested child class examples

 }
    public static XbeeInterface getInstance() throws TooManyListenersException{
     if(myXbee == null) myXbee = new XbeeInterface();
     return myXbee;
    }
  
 public void sendCopterData(String str) throws IOException{
  uartXbee.output.write(str.getBytes());
  return;
 }

 private class xBeeSerialPortEventListener implements SerialPortEventListener{
  public synchronized void serialEvent(SerialPortEvent oEvent) {
  if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
   try {
    String str = uartXbee.input.readLine();
    //Send command to Quadcopter (FSM)
    //Logger
    } catch (Exception e) {
     System.err.println(e.toString()); // TODO
    }
   } else { // Ignore all the other eventTypes, but you should consider the other ones.
    System.out.println("Serial Port Event not handled received");
   }

  }
 }

} 