package ucl.LightHouse;

public class UdpResponse extends Response<Boolean> {

	@Override
	public void callback(Boolean parameter) {
		System.out.println(parameter);
	}
}
