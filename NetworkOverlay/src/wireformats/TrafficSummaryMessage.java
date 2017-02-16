package wireformats;

import java.nio.ByteBuffer;

public class TrafficSummaryMessage extends Message{
    int numSent;
    long sumSent;

    int numReceived;
    long sumReceived;

    int numRelayed;

    String host;
    int port;

    public TrafficSummaryMessage(int _numSent, long _sumSent, int _numReceived, long _sumReceived, int _numRelayed, String _host, int _port) {
        numSent = _numSent;
        sumSent = _sumSent;

        numReceived = _numReceived;
        sumReceived = _sumReceived;

        numRelayed = _numRelayed;

        host = _host;
        port = _port;

        type = MessageType.TRAFFIC_SUMMARY;

        buildMessage();
    }

    public void buildMessage(){
        byteLength = 4 + 4 + 4 + 8 + 4 + 8 + 4 + 4 + host.getBytes().length;
        ByteBuffer bb = ByteBuffer.allocate(byteLength);

        bb.putInt(byteLength);
        bb.putInt(type.ordinal());

        bb.putInt(numSent);
        bb.putLong(sumSent);

        bb.putInt(numReceived);
        bb.putLong(sumReceived);

        bb.putInt(numRelayed);

        bb.putInt(port);
        bb.put(host.getBytes());

        message = bb.array();
    }
}
