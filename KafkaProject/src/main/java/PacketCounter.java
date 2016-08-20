import org.apache.spark.Accumulator;
import org.apache.spark.api.java.JavaSparkContext;

class PacketCounter {

	private static volatile Accumulator<Integer> instance = null;

	public static Accumulator<Integer> getInstance(JavaSparkContext jsc) {
		if (instance == null) {
			synchronized (PacketCounter.class) {
				if (instance == null) {
					instance = jsc.accumulator(0, "PacketCounter");
				}
			}
		}
		return instance;
	}
}