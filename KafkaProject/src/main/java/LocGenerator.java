import java.util.Random;

import org.json.JSONObject;

public class LocGenerator {

	private Random _rnd = new Random();
	private int _id = 0;

	private double getRandomDouble(double rangeMin, double rangeMax)
	{
		return rangeMin + (rangeMax - rangeMin) * _rnd.nextDouble();
	}
	private String getSaltString() {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 18) {
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;
	}

	public JSONObject generateLocJSON()
	{
		JSONObject obj = new JSONObject();

		obj.put("sensor_type", "TestLightHouse");
		obj.put("ID", _id);
		obj.put("Latitue", getRandomDouble(51.52,51.53));
		obj.put("Longitude", getRandomDouble(-0.12,-0.14));
		obj.put("x_axis", getRandomDouble(0, 100));
		obj.put("y_axis", getRandomDouble(0, 100));
		obj.put("Accuracy", getRandomDouble(0, 2));
		obj.put("map_ID", getSaltString());

		_id++;

		return obj;        
	}
}