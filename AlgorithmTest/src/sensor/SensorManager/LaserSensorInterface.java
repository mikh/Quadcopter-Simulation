import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.TooManyListenersException;
import java.io.*;

public class LaserSensorInterface{
	private UartDriver2 uart;
	private String UART_PORT_NAME = "/dev/ttyO4";
	private SensorManager theBoss;
	private int ID;

	public LaserSensorInterface(String port) throws TooManyListenersException{
		UART_PORT_NAME = port;
		uart = new UartDriver2(UART_PORT_NAME);
		uart.initialize();
		uart.serialPort.addEventListener(new LaserSerialPortEventListner());
	}

	private void sendRangeCommand() throws IOException{
		uart.output.write("D".getBytes());
	}

	private class LaserSerialPortEventListner implements SerialPortEventListener{
		public synchronized void serialEvent(SerialPortEvent oEvent){
			String str = "";
			if(oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE){
				try{
					str = uart.input.readLine();
					if(!str.equals("--.--"))
						theBoss.addRange(Double.parseDouble(str), ID);
					else
						theBoss.addRange(-1, ID);
				} catch(Exception e){
					System.out.println("Read exception occurred. str = " + str);
					e.printStackTrace();
				}
			} else{
				System.out.println("Not a DATA_AVAILABLE event.");
			}
		}
	}

	public void getRanging(){
		if(theBoss == null) theBoss = SensorManager.getInstance();
		try{
			sendRangeCommand();
		} catch(Exception e){
			System.out.println("");
		}
	}

	public void giveID(int ID){
		this.ID = ID;
	}
/*
	public static void main(String[] args){
		try{
			LaserSensorInterface lsi = LaserSensorInterface.getInstance();
			lsi.getRanging();
		} catch(Exception e){
			System.out.println("Exception occured");
			e.printStackTrace();
		}
	}
	*/
}