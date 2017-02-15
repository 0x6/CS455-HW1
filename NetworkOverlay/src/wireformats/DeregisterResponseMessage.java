package wireformats;

import java.nio.ByteBuffer;

public class DeregisterResponseMessage extends Message{
    byte status;
    String additionalInfo;

    public DeregisterResponseMessage(byte _status, String _additional){
        status = _status;
        additionalInfo = _additional;

        type = MessageType.DEREGISTER_RESPONSE;
        buildMessage();
    }

    @Override
    public void buildMessage() {
        byteLength = 4 + 4 + 1 + additionalInfo.getBytes().length;
        ByteBuffer bb = ByteBuffer.allocate(byteLength);

        bb.putInt(byteLength);
        bb.putInt(type.ordinal());
        bb.put(status);
        bb.put(additionalInfo.getBytes());

        message = bb.array();
    }
}
