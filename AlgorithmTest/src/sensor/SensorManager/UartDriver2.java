import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;

import java.util.Enumeration;

// When creating a UartDriver it is important to create a Listener for it. No default Listener is provided
public class UartDriver2 {
	public UartDriver2(String str){
		port_name = str;
	}
	
 SerialPort serialPort;
 private String port_name;
        /** The port we're normally going to use. */
 private static final String PORT_NAMES[] = { 
   "/dev/tty.usbserial-A9007UX1", // Mac OS X
                        "/dev/ttyACM0", // Raspberry Pi
   "/dev/ttyUSB0", // Linux
   "COM15", // Windows
 };
 /**
 * A BufferedReader which will be fed by a InputStreamReader 
 * converting the bytes into characters 
 * making the displayed results codepage independent
 */
 public BufferedReader input;
 /** The output stream to the port */
 public OutputStream output;
 /** Milliseconds to block while waiting for port open */
 private static final int TIME_OUT = 2000;
 /** Default bits per second for COM port. */
 private static final int DATA_RATE = 9600;
 
 public void initialize() {
                // the next line is for Raspberry Pi and 
                // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
                //System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

  CommPortIdentifier portId = null;
  Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

  while (portEnum.hasMoreElements()) {
      CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
      if (currPortId.getName().equals(port_name)) { 
	        portId = currPortId;
	        System.out.println("Connected to " + currPortId.getName());
	        break;
      }
  }

  if (portId == null) {
   System.out.println("Could not find COM port."); // TODO throw
   return;
  }

  try {
   // open serial port, and use class name for the appName.
   serialPort = (SerialPort) portId.open(this.getClass().getName(),
     TIME_OUT);

   // set port parameters
   serialPort.setSerialPortParams(DATA_RATE,
     SerialPort.DATABITS_8,
     SerialPort.STOPBITS_1,
     SerialPort.PARITY_NONE);

   // open the streams
   input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
   output = serialPort.getOutputStream();
 
   // add event listeners
   //serialPort.addEventListener(this); // do this in your own code!
   serialPort.notifyOnDataAvailable(true);
  } catch (Exception e) {
   System.err.println(e.toString());
  }
 }

 /**
  * This should be called when you stop using the port.
  * This will prevent port locking on platforms like Linux.
  */
 public synchronized void close() {
  if (serialPort != null) {
   serialPort.removeEventListener();
   serialPort.close();
  }
 }

 /**
  * Handle an event on the serial port. Read the data and print it.
  */
 // Do in your own code!

}
