
/* This class interfaces with the RPI, which is connected to the DRS thermal camera and is running HOG
 * One of the future plans is to add feature detection to go through doors. This feature is not implemented */

public class IRCamera {
   private UartDriver2 uartRPI;
//   private Logger logger;
 
   private static IRCamera myIRCamera;
   private IRCamera() throws TooManyListenersException { 
     uartRPI = new UartDriver2("/dev/ttyO6");  // fix port
     uartRPI.initialize();
     uartRPI.serialPort.addEventListener(new IRCameraSerialPortEventListener()); // Throws TooManyListenersException. This is a nested child class examples
//     logger = Logger.getInstance();
   }
   public static IRCamera getInstance() throws TooManyListenersException{
     if(myIRCamera == null) myIRCamera = new IRCamera();
     return myIRCamera;
   }
   
  private class IRCameraSerialPortEventListener implements SerialPortEventListener{
    public synchronized void serialEvent(SerialPortEvent oEvent) {
      if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
        try {
          String str = uartRPI.input.readLine();
// logger.write(Timestamp+": IRCamer, Receive: " + str);
          // Check if positive, then call the appropiate function. It may be a good idea to only call the function if we detect something X times in a row
        } catch (Exception e) {
          System.err.println(e.toString()); // TODO
        }
      } else { // Ignore all the other eventTypes, but you should consider the other ones.
        System.out.println("Serial Port Event not handled received");
      }
      
    }
 }