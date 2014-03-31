import java.util.Queue;
import java.util.LinkedList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.*;
import gnu.io.*;


public class Laser_test{
	public static void main(String[] args){
		LaserSensorInterface lsi = new LaserSensorInterface("/dev/ttyO4");
		System.out.println(lsi.getRanging());
	}
}

class LaserSensorInterface{
	String serialPortName;
	public LaserSensorInterface(String serialPortName){
		this.serialPortName = serialPortName;
	}

	public double getRanging(){
		double range = -1;
	
		try{
			CommPortIdentifier.addPortName(serialPortName, CommPortIdentifier.PORT_SERIAL, null);
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
			if(portIdentifier == null){
				System.out.println("portIdentifier == null");
			}
			else if(portIdentifier.isCurrentlyOwned()){
				System.out.println("Port is currently in use");
			} else{
				byte[] buffer = new byte[32];
				int len = -1;
				int TIME_OUT = 2000;
				SerialPort serialPort = (SerialPort) portIdentifier.open("laser_sensor", TIME_OUT);
				serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				InputStream in = serialPort.getInputStream();
				OutputStream out = serialPort.getOutputStream();
				out.write("D".getBytes());
				while((len = in.read(buffer)) == -1);
				String str = new String(buffer, 0, len);
				range =  Double.parseDouble(str);
			}
		/*
			delay(50);
			boolean pass = false;		
			while(!pass){
				try{
					BufferedReader br = new BufferedReader(new FileReader(rangeFile));
					String line = br.readLine();
					range = Double.parseDouble(line);
					br.close();
					pass = true;
				} catch(IOException e){
					System.out.println("Error opening file for laser sensor range interface.");
				} catch(NullPointerException n){

				}
			}
			*/
		} catch(Exception e){
			System.out.println("Error Exception occured.");
			e.printStackTrace();
		}

	
		return range;
	}

	private void delay(long milliseconds){
		long start = System.currentTimeMillis();
		while(System.currentTimeMillis() - start < milliseconds);
	}
}