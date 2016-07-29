package ucl.LightHouse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * LightHouseAPI.java 
 * Purpose: Provides an interface to interact with the
 * LightHouse platform. The interaction consists of sending data from sensors to
 * the platform and of querying the platform's database of obstacle around a
 * location. Settings containing IP address, port listening for the sensor data
 * and the port to query the database are stored in config.properties file in
 * resources.
 *
 * @author Vesko
 * @version 1.0 27.08.2016
 */
public class LightHouseAPI {
	/**
	 * Sends data from sensors to the LightHouse platform asynchronously by
	 * using UDP. Since the data is sent asynchronously the result is return as
	 * a callback function which should be overwritten by extending the Response
	 * class. The server IP and port are specified in config.properties file.
	 *
	 * @param map
	 *            sensor data presented as a hash map
	 * @param response
	 *            class extending Response and implementing callback function
	 * @return void
	 */
	public void sendSensorDataAsync(HashMap<String, String> map, Response<Boolean> response) {
		LighthouseDTO dto = new LighthouseDTO(map);
		UdpPacketSender transfer = new UdpPacketSender();
		byte[] bytesToSend = dto.toBytes();

		transfer.sendPacketAsync(bytesToSend, response);
	}

	/**
	 * Sends data from sensors to the LightHouse platform synchronously by using
	 * UDP. The server IP and port are specified in config.properties file.
	 *
	 * @param map
	 *            sensor data presented as a hash map
	 * @return result of the sending
	 */
	public boolean sendSensorDataSync(HashMap<String, String> map) {
		LighthouseDTO dto = new LighthouseDTO(map);
		UdpPacketSender transfer = new UdpPacketSender();
		byte[] bytesToSend = dto.toBytes();

		transfer.sendPacketSync(bytesToSend);

		return true;
	}

	/**
	 * Queries obstacles within a radius in a particular location from the
	 * LightHouse database synchronously. The server IP and obstacle api port
	 * are specified in config.properties file.
	 *
	 * @param longitude
	 *            decimal degrees of longitude
	 * @param latitude
	 *            decimal degrees of latitude
	 * @param radius
	 *            radius in meters within which obstacles should be return.
	 *            Cannot be more than 100 meters.
	 * @return ArrayList of obstacles
	 */
	public ArrayList<Obstacle> queryDatabaseSync(double longitude, double latitude, double radius) {
		ObstacleQuery query = new ObstacleQuery();

		ArrayList<Obstacle> obstacles = query.querySync(longitude, latitude, radius);

		return obstacles;
	}

	/**
	 * Queries obstacles within a radius in a particular location from the
	 * LightHouse database asynchronously. Since the data is sent asynchronously
	 * the result is return as a callback function which should be overwritten
	 * by extending the Response class. The server IP and obstacle api port are
	 * specified in config.properties file.
	 *
	 * @param longitude
	 *            decimal degrees of longitude
	 * @param latitude
	 *            decimal degrees of latitude
	 * @param radius
	 *            radius in meters within which obstacles should be return.
	 *            Cannot be more than 100 meters.
	 * @param response
	 *            class extending Response and implementing callback function
	 * @return ArrayList of obstacles
	 */
	public void queryDatabaseAsync(double longitude, double latitude, double radius,
			Response<ArrayList<Obstacle>> response) {
		ObstacleQuery query = new ObstacleQuery();

		query.queryAsync(longitude, latitude, radius, response);
	}
}
