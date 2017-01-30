import java.net.Socket;

public class MessagingNode {
	public static void main(String[] args){
		Socket socket;
		
		try{
			socket = new Socket();
		} catch(Exception e){
			System.out.println("Exception trying to initialize Socket: " + e);
		}
	}
}
