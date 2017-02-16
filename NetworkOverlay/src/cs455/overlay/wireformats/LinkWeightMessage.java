package cs455.overlay.wireformats;

import java.nio.ByteBuffer;

public class LinkWeightMessage extends Message{
    String[] links;

    public LinkWeightMessage(String[] _links){
        links = _links;
        type = MessageType.LINK_WEIGHTS;

        buildMessage();
    }

    public void buildMessage() {
        byteLength = 4 + 4 + 4;
        for(String str: links){
            byteLength += str.getBytes().length + 4;
        }

        ByteBuffer buffer = ByteBuffer.allocate(byteLength);

        buffer.putInt(byteLength);
        buffer.putInt(type.ordinal());
        buffer.putInt(links.length);

        for(String str: links){
            buffer.putInt(str.getBytes().length);
            buffer.put(str.getBytes());
        }

        message = buffer.array();
    }
}
