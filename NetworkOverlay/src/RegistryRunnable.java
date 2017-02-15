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

public class RegistryRunnable implements Runnable{
	public Socket clientSocket;
	public DataOutputStream dos;
	public DataInputStream dis;
	public HashMap<String, Socket> registry;

	public RegistryRunnable(Socket _clientSocket, HashMap<String, Socket> _registry){
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
					
					sanitizeMessages(message);
				}

				Thread.sleep(10);
			}
		} catch (Exception e) {
			System.out.println("Unable to get available bytes. " + e);
		}
		
		System.out.println("Done");
	}

	public void sanitizeMessages(byte[] bytestring){
		while(bytestring.length > 0){
			ByteBuffer buffer = ByteBuffer.wrap(bytestring);

			int length = buffer.getInt();
			byte[] message = Arrays.copyOfRange(bytestring, 4, length);

			handleMessage(message);

			bytestring = Arrays.copyOfRange(bytestring, length, bytestring.length);
		}
	}
	
	public void handleMessage(byte[] message) {
		ByteBuffer buffer = ByteBuffer.wrap(message);
		MessageType type = MessageType.values()[buffer.getInt()];

		switch (type) {
			case REGISTER_REQUEST:
				registerRequest(buffer.getInt(4), new String(Arrays.copyOfRange(message, 8, message.length)));
				break;
			case DEREGISTER_REQUEST:
				deregisterRequest(buffer.getInt(4), new String(Arrays.copyOfRange(message, 8, message.length)));
				break;
			case TASK_COMPLETE:
				System.out.println("Task complete.");
				break;
		}
	}
	
	public void registerRequest(int port, String host){
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
				System.out.println("[Registry] " + host + ":" + port + " registered.");
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
		byte status = 0;
		String additionalInfo = "";

		if(host.equals(clientSocket.getInetAddress().getHostAddress())){
			if(registry.keySet().contains(new String(host + ":" + port))){
				status = (byte)0;
				additionalInfo = "Node successfully deregistered.";

				registry.remove(new String(host + ":" + port));
				System.out.println("[Registry] " + host + ":" + port + " deregistered.");
			} else {
				status = (byte)2;
				additionalInfo = "No registration found for " + host + ":" + port + ".";
			}
		} else {
			status = (byte)1;
			additionalInfo = "Host name did not match.";
		}

		try {
			DeregisterResponseMessage resResponse = new DeregisterResponseMessage(status, additionalInfo);
			dos.write(resResponse.getMessage());
		} catch (IOException e) {
			System.out.println("Unable to send deregister response message. " + e);
		}
	}
}
