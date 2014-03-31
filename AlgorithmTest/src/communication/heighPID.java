import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;

class HeightPID {
  public int getPrevThrot() { return prevThrottle; }
  
  private int desAlt;
  //private int errorAlt;
  private int prevErrorAlt = 0; //set to zero for start
  private double errorIntegral = 0.0f; //errorIntegral starts at 0
  //private double addPAlt;
  //private double addIAlt;
  //private double addDAlt;
  //private int addToThrottle;
  private int prevThrottle = 0; //throttle starts at 0
  private double prevTime = 0;
  
  //tune these values
  final int throttleMax = 1000; //very high, will change
  final int throttleMin = 130;
  final int throttleScale = 100;
  final double kP = 0.1;
  final double kI = 0.02;
  final double kD = 0.005;
  final double timeStamp = 0.001;
  
   public void setDesiredAlt(int v) { desAlt = v; }
  
  //set Throttle with PID control and scaling to RC control values
  public void setThrottleWithAltitude(int current_altitude) {
    // Calculate time since last read 
    double currentTime = (double)System.currentTimeMillis();
    double timeDiff = currentTime - prevTime;
    
    //Calculate the error
    int errorAlt = desAlt - current_altitude;

    //Data collection for discrete time integration, limit data to 1000 entries
    errorIntegral += timeStamp*((prevErrorAlt+errorAlt)/2.0); //add midpoint approximation to total error integral

    //Data for differentiation
    int differentialAlt = errorAlt - prevErrorAlt;

    //adding the PID to current throttle command
    double addPAlt = kP*(errorAlt);
    double addIAlt = kI*errorIntegral;
    double addDAlt = kD*(differentialAlt);
    int newThrottle = prevThrottle + throttleScale*(int)(addPAlt+addIAlt+addDAlt);
    
    // Boundry check the new Throttle
    if(newThrottle > throttleMax || throttleMax < throttleMin){
      System.out.println("Throttle wrong! " + new Integer(newThrottle).toString());
    }
    
    System.out.println("new Throttle %d" + new Integer(newThrottle).toString());
    System.out.println("timeDiff %f" + Double.toString(timeDiff));
    System.out.println("Error Integral $f"+ Double.toString(errorIntegral));
    System.out.println("differentialAlt %f"+ Double.toString(differentialAlt));
     
    prevThrottle += newThrottle;
    prevErrorAlt = errorAlt; 
    prevTime = currentTime;
  }
  
  public static void main (String[] args) {
    HeightPID hpid = new HeightPID();
    int desiredAlt = 60;
    hpid.setDesiredAlt(desiredAlt);
    
    int throttle = 0;
    int height = 0;
    while(height != desiredAlt) {
      throttle = hpid.getPrevThrot();
      height += (throttle-450)/8;
      hpid.setThrottleWithAltitude(height);
      try {
    Thread.sleep(1000);
} catch(InterruptedException ex) {
    Thread.currentThread().interrupt();
}
    }
  }
}
  