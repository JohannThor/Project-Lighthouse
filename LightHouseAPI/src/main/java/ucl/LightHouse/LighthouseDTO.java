package ucl.LightHouse;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * LighthouseDTO.java Purpose: Class aimed to convert hash map to a JSONObject
 * and the JSONObject to array of bytes. It is used in sending sensor data as
 * byte array through UDP.
 *
 * @author Vesko
 * @version 1.0 27.08.2016
 */
class LighthouseDTO {

	private Map<String, String> data; // sensor data presented as a hash map

	/**
	 * Class constructor
	 *
	 * @param map
	 *            sensor data presented as a hash map
	 */
	public LighthouseDTO(Map<String, String> data) {
		this.data = new HashMap<String, String>(data);
	}

	/**
	 * Converts the hash map data to a JSONObject.
	 *
	 * @return JSONObject containing all of the data in hash map
	 */
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		Iterator<?> it = this.data.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
			obj.put(pair.getKey(), pair.getValue());
		}
		return obj;
	}

	/**
	 * Converts the object's hash map to JSONObject and then json string to byte
	 * array.
	 *
	 * @return byte array of the json string
	 */
	public byte[] toBytes() {
		JSONObject obj = this.toJSONObject();

		return obj.toString().getBytes();
	}

	/**
	 * Provides the object's hash map
	 *
	 * @return object's hash map of sensor data
	 */
	public Map<String, String> getData() {
		return this.data;
	}
}