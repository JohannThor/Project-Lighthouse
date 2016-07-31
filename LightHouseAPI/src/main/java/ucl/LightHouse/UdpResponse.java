package ucl.LightHouse;

/**
 * UdpResponse.java 
 * Purpose: Simple example of how to extend Response class
 * which is needed for the asynchronous functions.
 *
 * @author Vesko
 * @version 1.0 27.08.2016
 */
public class UdpResponse extends Response<Boolean> {

	/**
	 * Callback function which will be called when the asynchronous task is
	 * performed.
	 *
	 * @param parameter
	 *            list of obstacles which will be returned by the task.
	 * 
	 * @return void
	 */
	@Override
	public void callback(Boolean parameter) {
		System.out.println(parameter);
	}
}
