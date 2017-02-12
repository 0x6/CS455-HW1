import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Registry {
	final static int PORT = 8000;
	static ArrayList<String> registry;
	
	public static void main(String[] args) throws IOException{
		ServerSocket serverSocket = new ServerSocket(PORT);
		
		while(true){
			System.out.println("[Main] Waiting for connection...");
			Socket client = serverSocket.accept();
			System.out.println("[Main] Accepted.. " + client.getInetAddress() + " " + client.getPort());
			handleConnection(client);
		}
		
		/*try{
			serverSocket = new ServerSocket(PORT);
		} catch( Exception e){
			System.out.print("Exception trying to initialize ServerSocket: " + e);
		}*/
	}
	
	public static void handleConnection(Socket clientSocket){
		SocketRunnable r = new SocketRunnable(clientSocket, registry);
		new Thread(r).start();
	}
	
	public static void readMessage(int msgType){
		System.out.println(msgType);
	}
	
	public void register(){
		
	}
}
