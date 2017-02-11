import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MessagingNode {
	public static void main(String[] args) throws UnknownHostException, IOException{
		/*Socket client = new Socket("localhost", 8000);
		Scanner sc = new Scanner(System.in);

		DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		DataInputStream dis = new DataInputStream(client.getInputStream());*/
		
		String test = "127.0.0.1";
		
		byte[] b = test.getBytes();
		
		for(byte temp: b){
			System.out.println(temp + " " + (char) temp);
		}
		
		/*while(true){
			
		}*/
		
		/*try{
			socket = new Socket();
		} catch(Exception e){
			System.out.println("Exception trying to initialize Socket: " + e);
		}*/
	}
}
