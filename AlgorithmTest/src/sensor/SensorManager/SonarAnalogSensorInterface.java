import java.io.*;

public class SonarAnalogSensorInterface{
	String port;
	private int ID;
	private SensorManager theBoss;

	public SonarAnalogSensorInterface(String port){
		this.port = port;
		theBoss = SensorManager.getInstance();
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

		theBoss.addRange(rr, ID);
	}

	public void giveID(int ID){
		this.ID = ID;
	}

/*
	public static void main(String[] args){
		SonarAnalogSensorInterface sa = new SonarAnalogSensorInterface("/sys/devices/ocp.2/helper.14/AIN1");
		while(true){
			System.out.print(sa.getRanging());
			System.out.println(" cm.");
			try{Thread.sleep(30);} catch(Exception e){}
			try{
				Thread.sleep(500);
			} catch(Exception e){

			}
		}
	}
*/
}
