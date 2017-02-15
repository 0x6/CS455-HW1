package wireformats;

abstract public class Message {
	public enum MessageType{
		REGISTER_REQUEST,
		REGISTER_RESPONSE,
		DEREGISTER_REQUEST,
		DEREGISTER_RESPONSE,
		MESSAGING_NODES_LIST,
		LINK_WEIGHTS,
		TASK_INITIATE,
		TASK_COMPLETE,
		PULL_TRAFFIC_SUMMARY,
		TRAFFIC_SUMMARY,
		TEST_MESSAGE,
		DATA_TRANSMISSION
	}
	
	public byte[] message;
	public int byteLength;
	
	Message.MessageType type;
	
	abstract public void buildMessage();
	
	public byte[] getMessage(){
		return message;
	}
	public int getLength(){
		return byteLength;
	}
}
