package ucl.LightHouse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class PropertiesRetriever {
	
	private static PropertiesRetriever instance = null;
	   protected PropertiesRetriever() {
	      // Exists only to defeat instantiation.
	   }
	   public static PropertiesRetriever getInstance() {
	      if(instance == null) {
	         instance = new PropertiesRetriever();
	      }
	      return instance;
	   }
	   
	public String getServerIP(String propFileName) throws IOException
	{
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
	
	public int getServerPort(String propFileName) throws IOException, NumberFormatException
	{
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
	
	public int getApiPort(String propFileName) throws IOException
	{
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
