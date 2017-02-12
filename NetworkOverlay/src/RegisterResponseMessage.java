import java.nio.ByteBuffer;

public class RegisterResponseMessage extends Message{
	
	public RegisterResponseMessage(String _host, int _port){
		super(_host, _port);
		type = MessageType.DEREGISTER_REQUEST;
	}
	
	@Override
	public void buildMessage() {
		byteLength = host.getBytes().length + 8;
		ByteBuffer bb = ByteBuffer.allocate(byteLength);
		
		bb.putInt(type.ordinal());
		bb.putInt(port);
		bb.put(host.getBytes());
		
		message = bb.array();
	}
}
