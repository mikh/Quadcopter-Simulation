import java.lang.Process;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Exception;



public class SonarGPIOSensorInterface{
	String filename;
	final String C_CALL_NAME = "s_gpio";
	private int ID;
	private SensorManager theBoss;

	public SonarGPIOSensorInterface(String filename){
		this.filename = filename;
		theBoss = SensorManager.getInstance();
	}

	public double getRanging(){
		double range = -1;
		String s;
		try{
			Process p = Runtime.getRuntime().exec("./" + C_CALL_NAME + " " + filename);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while((s = stdInput.readLine()) != null){
				break;
			}
			range = Double.parseDouble(s);
		} catch(Exception e){
			System.out.println("Exception occured.");
			e.printStackTrace();
		}
		
		theBoss.addRange(range, ID);
	}

	public void giveID(int ID){
		this.ID = ID;
	}

/*
	public static void main(String[] args){
		SonarGPIOSensorInterface sg = new SonarGPIOSensorInterface("sense.txt");
		System.out.println(sg.getRanging());
	}
	*/
}