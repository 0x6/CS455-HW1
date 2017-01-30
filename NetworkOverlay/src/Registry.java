import java.net.ServerSocket;

public class Registry {
	final static int PORT = 8000;
	
	public static void main(String[] args){
		ServerSocket serverSocket;
		
		try{
			serverSocket = new ServerSocket(PORT);
		} catch( Exception e){
			System.out.print("Exception trying to initialize ServerSocket: " + e);
		}
	}
}
