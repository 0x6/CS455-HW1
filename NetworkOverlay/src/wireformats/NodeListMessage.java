package wireformats;

import java.nio.ByteBuffer;

public class NodeListMessage extends Message{
    String[] nodes;

    public NodeListMessage(String[] _nodes){
        type = MessageType.MESSAGING_NODES_LIST;
        nodes = _nodes;

        buildMessage();
    }

    public void buildMessage() {
        byteLength = 4 + 4;
        for(String str: nodes){
            byteLength += str.getBytes().length + 4;
        }

        ByteBuffer buffer = ByteBuffer.allocate(byteLength);

        buffer.putInt(type.ordinal());
        buffer.putInt(nodes.length);

        for(String str: nodes){
            buffer.putInt(str.getBytes().length);
            buffer.put(str.getBytes());
        }

        message = buffer.array();
    }
}
