import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PropertyGetter {
	
	private String path;
	
	private PropertyGetter(){}
	public PropertyGetter(String path){
		this.path = path;
	}
	public Properties getProperty() throws Exception {
		Properties property = new Properties(); 
		InputStream input = getClass().getClassLoader().getResourceAsStream(this.path);
		
		try {				
			if(input != null)
				property.load(getClass().getClassLoader().getResourceAsStream(this.path));
			else
				property.load(new FileInputStream(this.path));
		}catch(Exception e){
			List<String> error = Arrays.asList("Problem loading property " + e.toString() + " at:" + new java.util.Date());
			new LogErrors("property_error.txt", error).logError();
		}
		return property;
	}
	public void setProperty(String path){
		this.path = path;
	}
}
