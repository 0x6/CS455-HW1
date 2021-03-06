package cs455.overlay.wireformats;

import java.nio.ByteBuffer;

public class RegisterRequestMessage extends Message{
	public String host;
	public int port;

	public RegisterRequestMessage(String _host, int _port) {
		host = _host;
		port = _port;
		type = MessageType.REGISTER_REQUEST;

		buildMessage();
	}
	
	public void buildMessage(){
		byteLength = host.getBytes().length + 4 + 4 + 4;
		ByteBuffer bb = ByteBuffer.allocate(byteLength);

		bb.putInt(byteLength);
		bb.putInt(type.ordinal());
		bb.putInt(port);
		bb.put(host.getBytes());
		
		message = bb.array();
	}
}
