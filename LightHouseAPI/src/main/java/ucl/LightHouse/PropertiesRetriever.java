package ucl.LightHouse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * PropertiesRetriever.java 
 * Purpose: Retreives values from config.properties
 * file. Implemeted as a singleton.
 *
 * @author Vesko
 * @version 1.0 27.08.2016
 */
class PropertiesRetriever {

	private static PropertiesRetriever instance = null;

	protected PropertiesRetriever() {
		// Exists only to defeat instantiation.
	}

	public static PropertiesRetriever getInstance() {
		if (instance == null) {
			instance = new PropertiesRetriever();
		}
		return instance;
	}

	/**
	 * Retrieve server's IP from config.properties
	 *
	 * @param propFileName
	 *            the name of the file to read from
	 * 
	 * @return server's IP as a string
	 */
	public String getServerIP(String propFileName) throws IOException {
		Properties prop = new Properties();

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}

		String strIP = prop.getProperty("serverIP");

		return strIP;
	}

	/**
	 * Retrieve server's port from config.properties
	 *
	 * @param propFileName
	 *            the name of the file to read from
	 * 
	 * @return server's port as a string
	 */
	public int getServerPort(String propFileName) throws IOException, NumberFormatException {
		Properties prop = new Properties();

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}

		String strPort = prop.getProperty("serverPort");

		return Integer.parseInt(strPort);
	}

	/**
	 * Retrieve server's database api port from config.properties
	 *
	 * @param propFileName
	 *            the name of the file to read from
	 * 
	 * @return server's database api port as a string
	 */
	public int getApiPort(String propFileName) throws IOException {
		Properties prop = new Properties();

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}

		String apiPort = prop.getProperty("apiPort");

		return Integer.parseInt(apiPort);
	}
}
