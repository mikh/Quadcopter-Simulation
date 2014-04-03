import java.io.IOException;
import java.util.TooManyListenersException;


public class PilotController implements Runnable {
	private Pilot pilot;
	private double desAlt;
	private volatile boolean shutdown = false;
	// Altitude PID variables
	private int prevThrottle; //throttle starts at 0
	private double prevErrorAlt; //set to zero for start
	private double errorIntegral; //errorIntegral starts at 0
	private double prevTime;

	// Constants
	// Altitude PID
	final int throttleMax = 500; //very high, will change
	final int throttleMin = 130;
	final int throttleDeltaMax = 10;
	final double throttleScale = 1;
	final double kP = 0.1;
	final double kI = 0.02;
	final double kD = 0.005;
	
	private void setThrottleWithAltitude(double current_altitude) {
	    // Calculate time since last read 
	    double currentTime = (double)System.currentTimeMillis();
	    double timeDiff = currentTime - prevTime;
	    
	    //Calculate the error
	    double errorAlt = desAlt - current_altitude;

	    //Data collection for discrete time integration, limit data to 1000 entries
	    errorIntegral += timeDiff*((prevErrorAlt+errorAlt)/2.0) / 1000000; //add midpoint approximation to total error integral

	    //Data for differentiation
	    double differentialAlt = errorAlt - prevErrorAlt;

	    //adding the PID to current throttle command
	    double addPAlt = kP*(errorAlt);
	    double addIAlt = kI*errorIntegral;
	    double addDAlt = kD*(differentialAlt);

	    //int throttleChange = throttleScale*(int)(addPAlt+addIAlt+addDAlt);
	    int throttleChange = (int)(throttleScale * addPAlt); // Remove for full PID
	    if(throttleChange > throttleDeltaMax) throttleChange = throttleDeltaMax;
	    if(throttleChange < -throttleDeltaMax) throttleChange = -throttleDeltaMax;
	    int newThrottle = prevThrottle + throttleChange;
	    
	    // Boundry check the new Throttle
	    if(newThrottle > throttleMax ){
			  //messageQueue.add(String.format("a%04d",0)); // Throttle to large. Slow land
	      newThrottle = throttleMax;
	    } else if(newThrottle < throttleMin) {
	      newThrottle = throttleMin;
	    }
	    
	    pilot.setThrottle(newThrottle);
	   // messageQueue.add(String.format("t%04d",newThrottle));
		//  System.out.println("newThrottle" + (int) newThrottle);

	    prevThrottle = newThrottle;
	    prevErrorAlt = errorAlt; 
	    prevTime = currentTime;
	  }

	public void setDesAlt(double v) { desAlt = v; }
	public void shutdownConroller() { shutdown = false; }
	
	// Private constructor
	private static PilotController myPilotController;
	private PilotController() throws TooManyListenersException { 
		pilot = Pilot.getInstance(); 
		desAlt = 0;
		prevErrorAlt = 0;
		errorIntegral = 0.0f;
		prevThrottle = 0;
		prevTime = (double)System.currentTimeMillis();
	}
	public static PilotController getInstance() throws TooManyListenersException {
		if(myPilotController == null) myPilotController = new PilotController();
		return myPilotController;
	}

	public void run() {
		   while (!shutdown) {
			   try {
				   setThrottleWithAltitude(50);
				   pilot.sync();
		           Thread.sleep(333);
		           
		           
		           // stabalize loop
		           //setThrottleWithAltitude(50);
		           //pilot.sync();
		           
		           
		           
		          } catch (InterruptedException e) {
		              // good practice
		              Thread.currentThread().interrupt();
		              return;
		          } catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		      }
		
	}
	
}
