import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Registry {
	final static int PORT = 8000;
	static HashMap registry = new HashMap();
	
	public static void main(String[] args) throws IOException{
		ServerSocket serverSocket = new ServerSocket(PORT);
		
		while(true){
			Socket client = serverSocket.accept();
			System.out.println("Accepted.. " + client.getInetAddress() + " " + client.getPort());
			
			DataInputStream dis = new DataInputStream(client.getInputStream());
			DataOutputStream dos = new DataOutputStream(client.getOutputStream());
			
			readMessage(dis.readInt());
		}
		
		/*try{
			serverSocket = new ServerSocket(PORT);
		} catch( Exception e){
			System.out.print("Exception trying to initialize ServerSocket: " + e);
		}*/
	}
	
	public static void readMessage(int msgType){
		System.out.println(msgType);
	}
	
	public void register(){
		
	}
}
