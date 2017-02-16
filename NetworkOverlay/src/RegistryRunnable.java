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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RegistryRunnable implements Runnable{
	public Socket clientSocket;
	public DataOutputStream dos;
	public DataInputStream dis;
	public HashMap<String, Socket> registry;

	public AtomicInteger completed;
	public TrafficReport trafficReport;

	public RegistryRunnable(Socket _clientSocket, HashMap<String, Socket> _registry, AtomicInteger _completed, TrafficReport _trafficReport){
		clientSocket = _clientSocket;
		registry = _registry;
		completed = _completed;
		trafficReport = _trafficReport;
		
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
				completed.getAndIncrement();

				if(completed.get() == registry.size()){
					new Thread(new Runnable(){
						@Override
						public void run(){
							System.out.println("[Registry] Waiting 5 seconds for messages to propagate...");

							try {
								Thread.sleep(5000);

								for (String key : registry.keySet()) {
									Socket s = registry.get(key);

									TrafficPullMessage request = new TrafficPullMessage();
									s.getOutputStream().write(request.getMessage());

								}
							} catch (Exception e) {
								System.out.println("Unable to pull traffic.");
							}

						}
					}).start();
				}
				break;
			case TRAFFIC_SUMMARY:
				int sent = buffer.getInt(4);
				long sumSent =  buffer.getLong(8);
				int received = buffer.getInt(16);
				long sumReceived = buffer.getLong(20);
				int relayed = buffer.getInt(28);

				int port = buffer.getInt(32);
				String host = new String(Arrays.copyOfRange(message, 36, message.length));

				if(!trafficReport.headerBuilt()){
					System.out.println("|          Node          |  # Messages Sent  | # Messages Received |      Sum of Sent     |    Sum of Received    | # Messages Relayed |");
				}
				String output = "";

				String temp = host + ":" + port;
				output += "| " + temp;
				for(int i = 0; i < 22 - temp.length(); i++){
					output += " ";
				}

				temp = sent + "";
				output += " |     " + temp;
				for(int i = 0; i < 10 - temp.length(); i++){
					output += " ";
				}

				temp = received + "";
				output += "    |      " + temp;
				for(int i = 0; i < 10 - temp.length(); i++){
					output += " ";
				}

				temp = sumSent + "";
				output += "     |  " + temp;
				for(int i = 0; i < 19 - temp.length(); i++){
					output += " ";
				}

				temp = sumReceived + "";
				output += " |  " + temp;
				for(int i = 0; i < 19 - temp.length(); i++){
					output += " ";
				}

				temp = relayed + "";
				output += "  |      " + temp;
				for(int i = 0; i < 10 - temp.length(); i++){
					output += " ";
				}
				output += "    |";

				//System.out.println(host + ":" + port + " Sent: " + sent + " Sum: " + sumSent + " Received: " + received + " Sum: " + sumReceived + " Relayed: " + relayed);

				System.out.println(output);

				trafficReport.contribute(sent, sumSent, received, sumReceived);
				if(trafficReport.getContributers() == registry.size()){
					System.out.println(trafficReport.toString());
					trafficReport = new TrafficReport();
				}
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
				System.out.println("[Registry] " + host + ":" + port + " registered. (" + registry.size() + ") nodes currently in overlay.");
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
