package ucl.LightHouse;

import java.util.HashMap;

/**
 * app.java
 * Purpose: Simple example how to use the API. Shows how to send data from sensors to the LightHouse platform
 * and how to query the LightHouse database for obstacle around a given location. Both methods can be
 * called in synchronous and asynchronous way.
 *
 * @author Vesko
 * @version 1.0 27.08.2016
 */
public class app {
	
	/**
	 * Gives a simple example of how to send data to the LightHouse platform
	 * synchronously and how to query the LightHouse database asynchronously.
	 * Both methods can be called in synchronous and asynchronous way.
	 *
	 * @param args
	 *            initial arguments of the program. Since it is example there
	 *            are no arguments.
	 * @return void
	 */
	public static void main(String[] args) {
		LightHouseAPI api = new LightHouseAPI();
		HashMap<String, String> map = new HashMap<String, String>();

		map.put("ID", "Broke your program");
		boolean result = api.sendSensorDataSync(map);

		api.queryDatabaseAsync(-0.131, 51.522, 10, new QueryResponse());
	}

}
