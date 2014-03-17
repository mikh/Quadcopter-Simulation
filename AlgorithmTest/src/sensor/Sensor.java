package sensor;

public class Sensor {
	public int ID;
	public String type;
	public double cutoff;
	public double max_distance;
	public double min_distance;
	public double rangingDelay;
	
	public double angle;		//angle of alignment on quadcopter, as measured in the polar coordinate system
	public double position;		//position from center of side, can be + or -. Used if multiple sensors have the same angle
	
	public double reading;
	public double distance;
	
	private boolean simulation;
	private SensorSimulator SenSim;
	private SensorInterface SenInt;
	
	public Sensor(int ID, String type){
		this.ID = ID;
		this.type = type;
		simulation = false;
	}
	
	public Sensor(int ID, String type, double cutoff, double angle, double position){
		this.ID = ID;
		this.type = type;
		this.cutoff = cutoff;
		this.angle = angle;
		this.position = position;
		simulation = false;
	}
	
	public void simulate(SensorSimulator SenSim){
		simulation = true;
		this.SenSim = SenSim;
	}
	
	public void setInterface(SensorInterface SenInt){
		this.SenInt = SenInt;
		simulation = false;
	}
	
	public void performRanging(){
		if(simulation)
			distance = SenSim.getRange();
		else
			distance = SenInt.getRange();
		if(distance > cutoff)
			distance = -1;
	}
	
}
