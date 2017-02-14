import wireformats.DeregisterRequestMessage;
import wireformats.Message.MessageType;
import wireformats.RegisterRequestMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

public class MessagingNode {
	public static String host;
	public static int port;
	
	public static Socket clientSocket;
	public static ServerSocket serverSocket;
	public static DataOutputStream clientDos;
	public static DataInputStream clientDis;

	static Thread registryInputThread = new Thread(new Runnable(){
        @Override
        public void run(){
            try{
                while(true){
                    if(clientDis.available() > 0){
                        byte[] message = new byte[clientDis.available()];
                        clientDis.readFully(message);

                        handleMessage(message);
                    }
                }
            } catch (Exception e){
                System.out.println("Unable to read from input stream.");
            }
        }

        public void handleMessage(byte[] message){
            ByteBuffer buffer = ByteBuffer.wrap(message);
            MessageType type = MessageType.values()[buffer.getInt()];

            switch(type){
                case REGISTER_RESPONSE:
                    if(buffer.get(4) == 0){
                        System.out.println("[SUCCESS] " + buffer.get(4) + ": " + new String(Arrays.copyOfRange(message, 5, message.length)));
                    } else {
                        System.out.println("[FAILURE] " + buffer.get(4) + ": " + new String(Arrays.copyOfRange(message, 5, message.length)));
                    }
                    break;
            }
        }
    });
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		boolean mainFlag = true;

		serverSocket = new ServerSocket(0);
		clientSocket = new Socket(args[0], new Integer(args[1]));

		clientDos = new DataOutputStream(clientSocket.getOutputStream());
		clientDis = new DataInputStream(clientSocket.getInputStream());
		
		host = clientSocket.getInetAddress().getHostAddress();
		port = clientSocket.getLocalPort();

        registryInputThread.start();

		register();

		Scanner sc = new Scanner(System.in);
		do{
		    String command = sc.nextLine();

            switch(command){
                case "deregister":
                    deregister();
                    break;
                case "register":
                    register();
                    break;
                case "exit":
                    mainFlag = false;
                    break;
            }

		} while(mainFlag);

		clientSocket.close();
	}
	
	public static void register(){
	    System.out.println("Attempting to register...");
		RegisterRequestMessage request = new RegisterRequestMessage(host, port);
		
		try {
			clientDos.write(request.getMessage());
		} catch (IOException e) {
			System.out.print("Unable to register node. " + e);
		}
	}

	public static void deregister() {
		DeregisterRequestMessage request = new DeregisterRequestMessage(host, port);

		try {
			clientDos.write(request.getMessage());
		} catch (IOException e) {
			System.out.print("Unable to deregister node. " + e);
		}
	}
}
