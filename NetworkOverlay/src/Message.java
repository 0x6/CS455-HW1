abstract class Message {
	public enum MessageType{
		REGISTER_REQUEST,
		REGISTER_RESPONSE,
		DEREGISTER_REQUEST,
		MESSAGING_NODES_LIST,
		LINK_WEIGHTS,
		TASK_INITIATE,
		TASK_COMPLETE,
		PULL_TRAFFIC_SUMMARY,
		TRAFFIC_SUMMARY
	}
	
	public String host;
	public int port;
	public byte[] message;
	public int byteLength;
	
	Message.MessageType type;
	
	Message(String _host, int _port){
		host = _host;
		port = _port;
		
		buildMessage();
	}
	
	abstract public void buildMessage();
	
	public byte[] getMessage(){
		return message;
	}
	public int getLength(){
		return byteLength;
	}
}
