package wireformats;

import java.nio.ByteBuffer;

public class TrafficPullMessage extends Message{
    public TrafficPullMessage() {
        type = MessageType.PULL_TRAFFIC_SUMMARY;

        buildMessage();
    }

    public void buildMessage(){
        byteLength = 8;
        ByteBuffer bb = ByteBuffer.allocate(byteLength);

        bb.putInt(byteLength);
        bb.putInt(type.ordinal());

        message = bb.array();
    }
}
