import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class PropertyGetterTest {
	Properties props = new Properties();
	int port;
	@Before
	public void prepare() throws Exception{
		props = new PropertyGetter("producer.props").getProperty();
		port = Integer.parseInt(props.getProperty("UDPport"));
	}
	
	@Test
	public void test() throws Exception {
		assertNotNull(port);
		assertEquals(9998, port);
	}
}
