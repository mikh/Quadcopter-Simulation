


public class Quadcopter{
	public static void main(String[] args){
		Thread t = new Thread(SensorManager.getInstance());
		t.start();
	}
}