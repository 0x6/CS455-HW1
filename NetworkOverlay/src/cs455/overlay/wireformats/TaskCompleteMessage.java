package cs455.overlay.wireformats;

import java.nio.ByteBuffer;

public class TaskCompleteMessage extends Message{
    public String host;
    public int port;

    public TaskCompleteMessage(String _host, int _port) {
        host = _host;
        port = _port;
        type = MessageType.TASK_COMPLETE;

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
