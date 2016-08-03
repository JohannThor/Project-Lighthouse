package ucl.LightHouse.Interfaces;

import java.util.ArrayList;

import ucl.LightHouse.Obstacle;
import ucl.LightHouse.Response;

public interface IObstacleQuery {

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
	ArrayList<Obstacle> querySync(double longitude, double latitude, double radius);

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
	void queryAsync(double longitude, double latitude, double radius, Response<ArrayList<Obstacle>> response);

}