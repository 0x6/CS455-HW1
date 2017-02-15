import wireformats.DataTransmissionMessage;
import wireformats.DeregisterRequestMessage;
import wireformats.Message.MessageType;
import wireformats.RegisterRequestMessage;
import wireformats.TestMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class MessagingNode {
	public static String host;
	public static int port;
	
	public static Socket clientSocket;
	public static ServerSocket serverSocket;
	public static DataOutputStream clientDos;
	public static DataInputStream clientDis;

	public volatile static HashMap<String, Socket> connections;
	public static HashMap<String, ArrayList<Link>> links;

	static Thread inputThread = new Thread(new Runnable(){
        @Override
        public void run(){
            try{
                while(true){
                    if(clientDis.available() > 0){
                        byte[] message = new byte[clientDis.available()];
                        clientDis.readFully(message);

                        sanitizeMessages(message);
                    }

                    Thread.sleep(10);
                }
            } catch (Exception e){
                System.out.println("Unable to read from input stream." + e);
            }
        }

        public void sanitizeMessages(byte[] bytestring) throws IOException{
            while(bytestring.length > 0){
                ByteBuffer buffer = ByteBuffer.wrap(bytestring);

                int length = buffer.getInt();
                byte[] message = Arrays.copyOfRange(bytestring, 4, length);

                handleMessage(message);

                bytestring = Arrays.copyOfRange(bytestring, length, bytestring.length);
            }
        }

        public void handleMessage(byte[] message) throws IOException{
            ByteBuffer buffer = ByteBuffer.wrap(message);
            MessageType type = MessageType.values()[buffer.getInt()];

            switch(type){
                case REGISTER_RESPONSE:
                case DEREGISTER_RESPONSE:
                    System.out.println("[Node] " + buffer.get(4) + ": " + new String(Arrays.copyOfRange(message, 5, message.length)));
                    break;
                case MESSAGING_NODES_LIST:
                    handleLink(message);
                    break;
                case LINK_WEIGHTS:
                    handleWeights(message);
                    break;
                case TASK_INITIATE:
                    sendMessages(buffer.getInt(4));
                    break;
            }
        }
    });

	static Thread serverThread = new Thread(new Runnable(){
        @Override
        public void run(){
            while(true){
                try{
                    Socket connection = serverSocket.accept();

                    new Thread(new NodeConnectionRunnable(connection, connections)).start();
                } catch (IOException e){
                    System.out.println("Unable to accept incoming connection. " + e);
                }
            }
        }
    });
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		boolean mainFlag = true;
		connections = new HashMap<String, Socket>();
		links = new HashMap<String, ArrayList<Link>>();

		serverSocket = new ServerSocket(0);
		clientSocket = new Socket(args[0], new Integer(args[1]));

        host = clientSocket.getInetAddress().getHostAddress();
        port = serverSocket.getLocalPort();

		System.out.println("[Node] Identity: " + host + ":" + port);

		clientDos = new DataOutputStream(clientSocket.getOutputStream());
		clientDis = new DataInputStream(clientSocket.getInputStream());

        inputThread.start();
        serverThread.start();

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
                case "list-connections":
                    listConnections();
                    break;
                case "exit-overlay":
                    mainFlag = false;
                    break;
                case "test-compute":
                    ArrayList<String> list = new ArrayList<String>(links.keySet());
                    String source = host + ":" + port;
                    list.remove(source);

                    String sink = list.get(ThreadLocalRandom.current().nextInt(0, list.size()));

                    computePath(source, sink);
                    break;
            }

            if(command.contains("test-data")){
                String[] parts = command.split(" ");

                TestMessage testMessage = new TestMessage();
                if(connections.containsKey(parts[1]))
                    connections.get(parts[1]).getOutputStream().write(testMessage.getMessage());
            }

            if(command.contains("test-trans")){
                String[] parts = command.split(" ");

                String path = "";
                for(int i = 3; i < parts.length; i++){
                    path += parts[i] + " ";
                }
                path = path.trim();

                System.out.println("|" + path + "|");
                DataTransmissionMessage request = new DataTransmissionMessage(path, new Integer(parts[2]));
                if(connections.containsKey(parts[1]))
                    connections.get(parts[1]).getOutputStream().write(request.getMessage());
            }

		} while(mainFlag);

		clientSocket.close();
	}

	public static void listConnections(){
	    System.out.println("[Node] Number of connections: " + connections.size());
	    for(String key: connections.keySet()){
            System.out.println(key);
        }
    }

    public static void sendMessages(int numRounds){
        new Thread(new Runnable(){
            int rounds = numRounds;

            @Override
            public void run(){
                ArrayList<String> nodes = new ArrayList<String>(links.keySet());
                nodes.remove(host + ":" + port);

                for(int i = 0; i < rounds; i++){
                    String sink = nodes.get(ThreadLocalRandom.current().nextInt(0, nodes.size()));
                    String path = computePath(host + ":" + port, sink);

                    String[] parts = path.split(" ");
                    String target = parts[0];

                    path = "";
                    for(int j = 1; j < parts.length; j++){
                        path += parts[j];
                    }

                    try{
                        DataTransmissionMessage request = new DataTransmissionMessage(path, ThreadLocalRandom.current().nextInt());
                        connections.get(target).getOutputStream().write(request.getMessage());
                    } catch (IOException e){
                        System.out.print("Failed to send round.");
                    }
                }


            }
        }).start();
    }

    public static String computePath(String source, String sink){
        ArrayList<String> unvisited = new ArrayList<String>();
        HashMap<String, Integer> distance = new HashMap<String, Integer>();
        HashMap<String, String> previous = new HashMap<String, String>();

        boolean flag = false;

        for(String key: links.keySet()){
            distance.put(key, Integer.MAX_VALUE);
            previous.put(key, null);

            unvisited.add(key);
        }

        distance.put(host + ":" + port, 0);

        while(unvisited.size() > 0){
            String current = "";
            int lowestWeight = Integer.MAX_VALUE;

            for(String key: unvisited){
                if(distance.get(key) < lowestWeight){
                    lowestWeight = distance.get(key);
                    current = key;
                }
            }

            if(current.equals(sink)){
                flag = true;
                break;
            }
            unvisited.remove(current);

            ArrayList<Link> neighbors = links.get(current);
            for(Link neighbor: neighbors){
                int alt = distance.get(current) + neighbor.getWeight();

                if(alt < distance.get(neighbor.getNode())){
                    distance.put(neighbor.getNode(), alt);
                    previous.put(neighbor.getNode(), current);
                }
            }
        }

        String current = sink;
        String path = current;

        while(!previous.get(current).equals(source)){
            path = previous.get(current) + " " + path;
            current = previous.get(current);
        }

        /*String target = path.substring(0, path.indexOf(" "));
        String newPath = path.substring(path.indexOf(" ") + 1);

        DataTransmissionMessage request = new DataTransmissionMessage(newPath, 20);
        if(connections.containsKey(target)){
            try{
                connections.get(target).getOutputStream().write(request.getMessage());
            } catch (IOException e){
                System.out.println("Unable to send data to " + target + ".");
            }
        }*/

        return path;
    }

    public static void handleWeights(byte[] message){
	    ByteBuffer buffer = ByteBuffer.wrap(message);

        int numLinks = buffer.getInt(4);
        String[] messageLinks = new String[numLinks];

        int offset = 8;
        for(int i = 0; i < numLinks; i++){
            int hostLength = buffer.getInt(offset);
            messageLinks[i] = new String(Arrays.copyOfRange(message, offset + 4, offset + 4 + hostLength));
            //System.out.println(links[i]);
            offset += 4 + hostLength;
        }

        for(String link: messageLinks){
            String node1 = link.split(" ")[0];
            String node2 = link.split(" ")[1];

            Link link1 = new Link(node2, new Integer(link.split(" ")[2]));
            Link link2 = new Link(node1, new Integer(link.split(" ")[2]));

            ArrayList<Link> list1, list2;
            if(links.containsKey(node1)){
                list1 = links.get(node1);
            } else {
                list1 = new ArrayList<Link>();
            }

            if(links.containsKey(node2)){
                list2 = links.get(node2);
            } else {
                list2= new ArrayList<Link>();
            }

            list1.add(link1);
            list2.add(link2);

            links.put(node1, list1);
            links.put(node2, list2);
        }

        /*for(String key: links.keySet()){
            System.out.print(key + " | ");
            for(Link link: links.get(key)){
                System.out.print(link.toString() + " ");
            }
            System.out.println();
        }*/

        System.out.println("[Node] Link weights are received and processed. Ready to send messages.");
    }

	public static void handleLink(byte[] message) throws IOException{
        ByteBuffer buffer = ByteBuffer.wrap(message);

        int numHosts = buffer.getInt(4);
        String[] hosts = new String[numHosts];

        int offset = 8;
        for(int i = 0; i < numHosts; i++){
            int hostLength = buffer.getInt(offset);
            hosts[i] = new String(Arrays.copyOfRange(message, offset + 4, offset + 4 + hostLength));
            offset += 4 + hostLength;
        }

        for(String address: hosts){
            String[] parts = address.split(":");
            String cHost = parts[0];
            int cPort = new Integer(parts[1]);

            try{
                Socket connection = new Socket(cHost, cPort);
                connections.put(cHost + ":" + cPort, connection);
                System.out.println("[Node] Creating connection to " + cHost + ":" + cPort);

                new Thread(new NodeConnectionRunnable(connection, connections)).start();

                RegisterRequestMessage request = new RegisterRequestMessage(connection.getInetAddress().getHostAddress(), port);
                connection.getOutputStream().write(request.getMessage());
            } catch (IOException e){
                System.out.println("Unable to connect to " + cHost + ":" + cPort);
                throw e;
            }
        }
    }
	
	public static void register() throws IOException {
		RegisterRequestMessage request = new RegisterRequestMessage(host, port);
		
		try {
			clientDos.write(request.getMessage());
		} catch (IOException e) {
			System.out.print("Unable to register node. " + e);
			throw e;
		}
	}

	public static void deregister() throws IOException {
		DeregisterRequestMessage request = new DeregisterRequestMessage(host, port);

		try {
            clientDos.write(request.getMessage());
		} catch (IOException e) {
			System.out.print("Unable to deregister node. " + e);
			throw e;
		}
	}
}
