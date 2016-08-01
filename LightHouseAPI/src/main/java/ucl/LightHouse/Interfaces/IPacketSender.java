package ucl.LightHouse.Interfaces;

import ucl.LightHouse.Response;

public interface IPacketSender {

	/**
	 * Send array of bytes to the Lighthouse platform asynchronously. Since the
	 * data is sent asynchronously the result is return as a callback function
	 * which should be overwritten by extending the Response class.
	 *
	 * @param data
	 *            json string represented as array of bytes
	 * @param response
	 *            class extending Response and implementing callback function
	 * 
	 * @return void
	 */
	void sendPacketAsync(byte[] data, Response<Boolean> response);

	/**
	 * Send array of bytes to the Lighthouse platform synchronously.
	 *
	 * @param data
	 *            json string represented as array of bytes
	 * 
	 * @return result of the sending as a boolean
	 */
	boolean sendPacketSync(byte[] data);

}