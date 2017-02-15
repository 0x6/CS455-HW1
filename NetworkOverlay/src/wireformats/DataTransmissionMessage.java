package wireformats;

import java.nio.ByteBuffer;

public class DataTransmissionMessage extends Message{
    String path;
    int payload;

    public DataTransmissionMessage(String _path, int _payload){
        path = _path;
        payload = _payload;
        type = MessageType.DATA_TRANSMISSION;
        buildMessage();
    }

    @Override
    public void buildMessage() {
        byteLength = 4 + 4 + 4 + path.getBytes().length;
        ByteBuffer buffer = ByteBuffer.allocate(byteLength);

        buffer.putInt(byteLength);
        buffer.putInt(type.ordinal());
        buffer.putInt(payload);
        buffer.put(path.getBytes());

        message = buffer.array();
    }
}
