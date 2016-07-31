package ucl.LightHouse;

import java.util.ArrayList;

/**
 * QueryResponse.java 
 * Purpose: Simple example of how to extend Response class
 * which is needed for the asynchronous functions.
 *
 * @author Vesko
 * @version 1.0 27.08.2016
 */
public class QueryResponse extends Response<ArrayList<Obstacle>> {

	/**
	 * Callback function which will be called when the asynchronous task is
	 * performed.
	 *
	 * @param parameter
	 *            list of obstacles which will be returned by the task. If
	 *            parameter is null then there is an error in the task.
	 * 
	 * @return void
	 */
	@Override
	public void callback(ArrayList<Obstacle> parameter) {
		for (int i = 0; i < parameter.size(); i++) {
			System.out.println(parameter.get(i).toString());
		}
	}
}