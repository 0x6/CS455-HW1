import wireformats.NodeListMessage;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Registry {
	final static int PORT = 8000;
	static volatile HashMap<String, Socket> registry;
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
	
	public static void main(String[] args) throws IOException{
		boolean mainFlag = true;

		serverSocket = new ServerSocket(PORT);
		
		registry = new HashMap<String, Socket>();
		inputQueue = new ArrayList<String>();

		serverThread.start();

		Scanner sc = new Scanner(System.in);
		do{
			String command = sc.nextLine();

			switch(command){
				case "list-messaging nodes":
					System.out.println("[Main] Size of registry: " + registry.size());
					for(String str: registry.keySet()){
						System.out.println("[Main] " + str);
					}
					break;
				case "setup-overlay":
					setupOverlay(4);
					break;
				case "exit":
					mainFlag = false;
					break;
			}

		} while(mainFlag);
	}

	public static void setupOverlay(int connections){
		String[] list = registry.keySet().toArray(new String[registry.keySet().size()]);

		NodeListMessage request = new NodeListMessage(list);
		try{
			DataOutputStream temp = new DataOutputStream(registry.get(list[0]).getOutputStream());

			byte[] bytestr = request.getMessage();
			for(byte b: bytestr){
				//System.out.print("[" + b + "] ");
			}

			temp.write(request.getMessage());
		} catch (Exception e){
			System.out.println("Unable to write to output stream. " + e);
		}
	}
}
