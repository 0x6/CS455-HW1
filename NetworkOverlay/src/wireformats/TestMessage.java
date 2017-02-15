package wireformats;

import java.nio.ByteBuffer;

public class TestMessage extends Message  {

    public TestMessage(){
        type = MessageType.TEST_MESSAGE;
        buildMessage();
    }

    public void buildMessage(){
        ByteBuffer buffer = ByteBuffer.allocate(8);

        buffer.putInt(8);
        buffer.putInt(type.ordinal());

        message = buffer.array();
    }
}
