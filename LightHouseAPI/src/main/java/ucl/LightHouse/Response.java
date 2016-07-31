package ucl.LightHouse;

/**
 * Response.java 
 * Purpose: Provides a generic way of passing different callback
 * classes to the asynchronous functions. Gives the user ability to customise
 * the actions which will be taken after the asynchronous task has finished.
 *
 * @author Vesko
 * @version 1.0 27.08.2016
 */
public abstract class Response<T> {
	public abstract void callback(T parameter);
}
