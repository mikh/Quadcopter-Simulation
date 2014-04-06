import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.TooManyListenersException;
import java.io.*;

public class LaserSensorInterface{
	private UartDriver2 uart;
	private static LaserSensorInterface lsi;
	private final String UART_PORT_NAME = "/dev/ttyO4";

	private LaserSensorInterface() throws TooManyListenersException{
		uart = new UartDriver2(UART_PORT_NAME);
		uart.initialize();
		uart.serialPort.addEventListener(new LaserSerialPortEventListner());
	}

	public static LaserSensorInterface getInstance() throws TooManyListenersException{
		if(lsi == null) lsi = new LaserSensorInterface();
		return lsi;
	}

	private void sendRangeCommand() throws IOException{
		uart.output.write("D".getBytes());
	}

	private class LaserSerialPortEventListner implements SerialPortEventListener{
		public synchronized void serialEvent(SerialPortEvent oEvent){
			if(oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE){
				try{
					String str = uart.input.readLine();
					System.out.println(str);
				} catch(Exception e){
					System.out.println("Read exception occurred.");
					e.printStackTrace();
				}
			} else{
				System.out.println("Not a DATA_AVAILABLE event.");
			}
		}
	}

	public void getRanging(){
		try{
			sendRangeCommand();
		} catch(Exception e){
			System.out.println("");
		}
	}

	public static void main(String[] args){
		try{
			LaserSensorInterface lsi = LaserSensorInterface.getInstance();
			lsi.getRanging();
		} catch(Exception e){
			System.out.println("Exception occured");
			e.printStackTrace();
		}
	}
}