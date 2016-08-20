import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;

public class Producer {

	public static void main(String[] args) throws Exception{
		String path = "producer.props";
		Properties properties = new Properties();
		Integer port = null;
		properties = new PropertyGetter(path).getProperty();

		try {
			port = Integer.parseInt(properties.getProperty("UDPport"));
		}catch(NumberFormatException nfe){
			List<String> error = Arrays.asList("Please specify valid port number  "+nfe.toString() + " at:" + new java.util.Date());			
			new LogErrors("port_error.txt", error).logError();
		}
		
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);		
		PacketReceiver incomingData = new PacketReceiver(port);

		while (true) {	
			try {	
				String sensorDataAsString = new String(incomingData.returnByteBuffer());
				JSONObject sensorData = new JSONObject(sensorDataAsString);
				String topic = sensorData.get("sensor_type").toString();

				sensorData.remove("sensor_type");

				producer.send(new ProducerRecord<String, String>(topic, 
						topic, sensorData.toString()));			
			}catch(Exception e){
				List<String> error = Arrays.asList("error message "+e.toString() + " at:" + new java.util.Date());
				new LogErrors("error_reports_from_producer.txt", error).logError();
			}
		}
	}
}