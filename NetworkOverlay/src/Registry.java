import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Registry {
	final static int PORT = 8000;
	static volatile ArrayList<String> registry;
	static volatile ArrayList<String> inputQueue;
	static ServerSocket serverSocket;

	static Thread serverThread = new Thread(new Runnable(){
		@Override
		public void run(){
			while(true){
				try{
					Socket clientSocket = serverSocket.accept();
					System.out.println("[Server] Accepted.. " + clientSocket.getInetAddress() + " " + clientSocket.getPort());

					SocketRunnable r = new SocketRunnable(clientSocket, registry);
					new Thread(r).start();
				} catch (Exception e){
					System.out.println("Unable to accept incoming connection");
				}
			}
		}
	});

	static Thread inputThread = new Thread(new Runnable(){
		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);
			String command = "";

			do{
				command = sc.nextLine();
				inputQueue.add(command);

			} while (!command.equals("exit"));
		}
	});
	
	public static void main(String[] args) throws IOException{
		boolean mainFlag = true;

		serverSocket = new ServerSocket(PORT);
		
		registry = new ArrayList<String>();
		inputQueue = new ArrayList<String>();

		inputThread.start();
		serverThread.start();

		while(mainFlag){
			if(inputQueue.size() > 0){
				String command = inputQueue.get(0);
				inputQueue.remove(0);

				switch(command){
					case "list-messaging nodes":
						System.out.println("[Main] Size of registry: " + registry.size());
						for(String str: registry){
							System.out.println("[Main] " + str);
						}
						break;
					case "exit":
						mainFlag = false;
						break;
				}
			}
		}
	}
}
