import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

public class LogErrors {
	private String filePath = null;
	private List<String> error = null;
	
	public LogErrors(String filePath, List<String> error){
		this.filePath = filePath;
		this.error = error;
	}
	
	public void logError() throws Exception {
		try (FileWriter fw = new FileWriter(this.filePath, true);
				BufferedWriter writer = new BufferedWriter(fw)){
			
			for(String line : this.error){
				writer.append(line);
				writer.newLine();
			}
		}
	}
}
