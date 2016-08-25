package lighthouse.lighthouse;

class SensorReading {
	public SensorReading(Double tickTime, String[] sensorValue) {
		this.tickTime = tickTime;
		this.sensorValue = sensorValue;
	}
	Double tickTime;
	String[] sensorValue;
}