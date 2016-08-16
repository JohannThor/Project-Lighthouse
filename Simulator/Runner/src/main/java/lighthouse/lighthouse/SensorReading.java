package lighthouse.lighthouse;

class SensorReading {
	public SensorReading(Double tickTime, Double sensorValue, Double velocity) {
		this.tickTime = tickTime;
		this.sensorValue = sensorValue;
		this.velocity = velocity;
	}
	Double velocity;
	Double tickTime;
	Double sensorValue;
}