import wireformats.*;
import wireformats.Message.MessageType;

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
	public HashMap<String, Socket> registry;
	
	public SocketRunnable(Socket _clientSocket, HashMap<String, Socket> _registry){
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

				Thread.sleep(10);
			}
		} catch (Exception e) {
			System.out.println("Unable to get available bytes. " + e);
		}
		
		System.out.println("Done");
	}
	
	public void handleMessage(byte[] message){
		ByteBuffer buffer = ByteBuffer.wrap(message);
		MessageType type = MessageType.values()[buffer.getInt()];
		
		switch(type){
		case REGISTER_REQUEST:
			buffer = ByteBuffer.wrap(message, 4, 4);
			registerRequest(buffer.getInt(), new String(Arrays.copyOfRange(message, 8, message.length)));
			break;
		case DEREGISTER_REQUEST:
			buffer = ByteBuffer.wrap(message, 4, 4);
			deregisterRequest(buffer.getInt(), new String(Arrays.copyOfRange(message, 8, message.length)));
			break;
		}
	}
	
	public void registerRequest(int port, String host){
		byte[] message;
		byte status = 0;
		String additionalInfo = "";
		
		if(host.equals(clientSocket.getInetAddress().getHostAddress())){
			if(registry.keySet().contains(new String(host + ":" + port))){
				status = (byte)2;
				additionalInfo = "Messaging node is already registered.";
			} else {
				registry.put(new String(host + ":" + port), clientSocket);
				
				status = (byte)0;
				additionalInfo = "Node successfully registered.";
			}
					
		} else {
			status = (byte)1;
			additionalInfo = "Host name did not match.";
		}
		
		try {
			RegisterResponseMessage resResponse = new RegisterResponseMessage(status, additionalInfo);
			dos.write(resResponse.getMessage());
		} catch (IOException e) {
			System.out.println("Unable to send register response message. " + e);
		}
	}

	public void deregisterRequest(int port, String host){
		if(host.equals(clientSocket.getInetAddress().getHostAddress())){
			if(registry.keySet().contains(new String(host + ":" + port))){
				registry.remove(new String(host + ":" + port));
			} else {
				System.out.println("No registration found for host " + host + " " + port + ".");
			}
		} else {
			System.out.println("No registration found for host " + host + " " + port + ".");
		}
	}
}
