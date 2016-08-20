import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PacketReceiverTest {
	
	PacketReceiver testData;
	@Before
	public void prepare() throws Exception{
		testData = new PacketReceiver(9998);
	}
	
	@Test
	public void test() throws Exception {
		assertNotNull(testData.getPort());
	}
}
