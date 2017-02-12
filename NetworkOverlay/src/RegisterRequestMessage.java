import java.nio.ByteBuffer;

public class RegisterRequestMessage extends Message{
	
	RegisterRequestMessage(String _host, int _port) {
		super(_host, _port);
		type = MessageType.REGISTER_REQUEST;
	}
	
	public void buildMessage(){
		byteLength = host.getBytes().length + 8;
		ByteBuffer bb = ByteBuffer.allocate(byteLength);
		
		bb.putInt(type.ordinal());
		bb.putInt(port);
		bb.put(host.getBytes());
		
		message = bb.array();
	}
}
