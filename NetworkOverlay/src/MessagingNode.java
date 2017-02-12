import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;

public class MessagingNode {
	public static String host;
	public static int port;
	
	public static Socket clientSocket;
	public static DataOutputStream dos;
	public static DataInputStream dis;
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		host = args[0];
		port = new Integer(args[1]);
		
		clientSocket = new Socket(host, port);

		dos = new DataOutputStream(clientSocket.getOutputStream());
		dis = new DataInputStream(clientSocket.getInputStream());
		
		register();
		
		/*ByteBuffer bb = ByteBuffer.wrap(test, 0, 4);
		System.out.println(bb.getInt());
		
		bb = ByteBuffer.wrap(test, 4, 4);
		System.out.println(bb.getInt());
		
		System.out.println(new String(Arrays.copyOfRange(test, 8, test.length)));*/
		clientSocket.close();
	}
	
	public static void register(){
		RegisterRequestMessage request = new RegisterRequestMessage(host, port);
		
		try {
			dos.write(request.getMessage());
		} catch (IOException e) {
			System.out.print("Unable to register node. " + e);
		}
	}
}
