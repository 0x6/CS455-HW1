import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SocketRunnable implements Runnable{
	public Socket clientSocket;
	public DataOutputStream dos;
	public DataInputStream dis;
	public ArrayList<String> registry;
	
	public SocketRunnable(Socket _clientSocket, ArrayList<String> _registry){
		clientSocket = _clientSocket;
		registry = _registry;
		
		try {
			dos = new DataOutputStream(clientSocket.getOutputStream());
			dis = new DataInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("Unable to acquire data streams. " + e);
		}
	}
	
	@Override
	public void run() {
		try {
			boolean flag = true;
			while(flag){
				if(dis.available() > 0){
					byte[] message = new byte[dis.available()];
					dis.readFully(message);
					
					handleMessage(message);
				}
			}
		} catch (Exception e) {
			System.out.println("Unable to get available bytes. " + e);
		}
		
		System.out.println("Done");
	}
	
	public void handleMessage(byte[] message){
		ByteBuffer buffer = ByteBuffer.wrap(message);
		Message.MessageType type = Message.MessageType.values()[buffer.getInt()];
		
		switch(type){
		case REGISTER_REQUEST:
			buffer = ByteBuffer.wrap(message, 4, 4);
			registerRequest(buffer.getInt(), new String(Arrays.copyOfRange(message, 8, message.length)));
			break;
		case DEREGISTER_REQUEST:
			break;
		}
	}
	
	public void registerRequest(int port, String host){
		
	}
}
