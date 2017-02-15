package wireformats;

import java.nio.ByteBuffer;

public class TaskInitiateMessage extends Message  {
    int numRounds;

    public TaskInitiateMessage(int _numRounds){
        numRounds = _numRounds;
        type = MessageType.TASK_INITIATE;
        buildMessage();
    }

    public void buildMessage(){
        byteLength = 4 + 4 + 4;
        ByteBuffer buffer = ByteBuffer.allocate(byteLength);

        buffer.putInt(byteLength);
        buffer.putInt(type.ordinal());
        buffer.putInt(numRounds);

        message = buffer.array();
    }
}
