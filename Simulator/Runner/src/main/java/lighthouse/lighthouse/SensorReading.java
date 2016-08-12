package lighthouse.lighthouse;

class SensorReading {
	public SensorReading(Double tickTime, Double sensorValue) {
		this.tickTime = tickTime;
		this.sensorValue = sensorValue;
	}
	Double tickTime;
	Double sensorValue;
}