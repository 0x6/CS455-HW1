package wireformats;

import java.nio.ByteBuffer;

public class RegisterResponseMessage extends Message{
	byte status;
	String additionalInfo;
	
	public RegisterResponseMessage(byte _status, String _additional){
		status = _status;
		additionalInfo = _additional;
		
		type = MessageType.REGISTER_RESPONSE;
		buildMessage();
	}
	
	@Override
	public void buildMessage() {
		byteLength = 4 + 1 + additionalInfo.getBytes().length;
		ByteBuffer bb = ByteBuffer.allocate(byteLength);
		
		bb.putInt(type.ordinal());
		bb.put(status);
		bb.put(additionalInfo.getBytes());
		
		message = bb.array();
	}
}
