package ucl.LightHouse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

/**
 * ObstacleQuery.java 
 * Purpose: Query the Lighthouse platform's database for
 * Obstacles within a radius for a particular location.
 *
 * @author Vesko
 * @version 1.0 27.08.2016
 */

class ObstacleQuery {
	private String _serverIP; // Server's IP to query the database
	private int _apiPort; // Server's port to query the database

	/**
	 * ObstacleQuery constructor which assigns the server's IP and port
	 * according to the settings in config.properties file.
	 *
	 */
	public ObstacleQuery() {
		try {
			_serverIP = PropertiesRetriever.getInstance().getServerIP("config.properties");
			_apiPort = PropertiesRetriever.getInstance().getApiPort("config.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sanitizes the radius input to be less than 100 meters. Done to ensure
	 * that there are no queries for huge amount of obstacles.
	 *
	 * @param radius
	 *            radius in meters within the location
	 * 
	 * @return true if radius is within 100 meters
	 */
	private boolean checkRadius(double radius) {
		if (radius > 0 && radius < 100)
			return true;

		return false;
	}

	/**
	 * Deserialize the json received from the Lighthouse platform's database
	 * into list of Obstacle objects
	 *
	 * @param response
	 *            list of obstacles in json format received from querying
	 *            Lighthouse database
	 * 
	 * @return ArrayList of obstacles
	 */
	private ArrayList<Obstacle> deserialize(String response) {
		JSONObject jsonObj = new JSONObject(response);

		ArrayList<Obstacle> obstacles = ObstacleDeserializer.deserializeResponse(jsonObj);

		return obstacles;
	}

	/**
	 * Deserialize the json received from the Lighthouse platform's database
	 * into list of Obstacle objects synchronously.
	 *
	 * @param longitude
	 *            decimal degrees of longitude
	 * @param latitude
	 *            decimal degrees of latitude
	 * @param radius
	 *            radius in meters within which obstacles should be return.
	 *            Cannot be more than 100 meters.
	 * 
	 * @return ArrayList of obstacles
	 */
	public ArrayList<Obstacle> querySync(final double longitude, final double latitude, final double radius) {
		final List<Obstacle> obstacles = Collections.synchronizedList(new ArrayList<Obstacle>());
		if (!checkRadius(radius))
			return null;

		Thread t = new Thread(new Runnable() {
			public void run() {
				try {

					URL url = new URL("http://" + _serverIP + ":" + _apiPort + "/ObstacleAPI/v0_1/Locations/Query?"
							+ "longitude=" + longitude + "&" + "latitude=" + latitude + "&" + "radius=" + radius);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Accept", "application/json");

					if (conn.getResponseCode() == 200) {
						String response = IOUtils.toString(conn.getInputStream());
						obstacles.addAll(deserialize(response));
						conn.disconnect();
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return new ArrayList<>(obstacles);
	}

	/**
	 * Deserialize the json received from the Lighthouse platform's database
	 * into list of Obstacle objects. Since the data is sent asynchronously the
	 * result is return as a callback function which should be overwritten by
	 * extending the Response class.
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
	 *            
	 * @return void
	 */
	public void queryAsync(final double longitude, final double latitude, final double radius,
			final Response<ArrayList<Obstacle>> response) {

		final List<Obstacle> obstacles = Collections.synchronizedList(new ArrayList<Obstacle>());
		if (!checkRadius(radius))
			response.callback(null);

		Thread t = new Thread(new Runnable() {
			public void run() {
				try {

					URL url = new URL("http://" + _serverIP + ":" + _apiPort + "/ObstacleAPI/v0_1/Locations/Query?"
							+ "longitude=" + longitude + "&" + "latitude=" + latitude + "&" + "radius=" + radius);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Accept", "application/json");

					if (conn.getResponseCode() == 200) {

						String getResponse = IOUtils.toString(conn.getInputStream());
						obstacles.addAll(deserialize(getResponse));
						conn.disconnect();
						response.callback(new ArrayList<>(obstacles));
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
					response.callback(null);
				} catch (IOException ex) {
					ex.printStackTrace();
					response.callback(null);
				}
			}
		});

		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
}
