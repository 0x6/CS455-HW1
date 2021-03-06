package cs455.overlay.node;

import cs455.overlay.wireformats.LinkWeightMessage;
import cs455.overlay.wireformats.NodeListMessage;
import cs455.overlay.wireformats.TaskInitiateMessage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Registry {
	static volatile HashMap<String, Socket> registry;
	static ServerSocket serverSocket;

	static HashMap<String, ArrayList<String>> partners;
	static AtomicInteger completed;

	static ArrayList<String> links;
	static TrafficReport trafficReport;

	static Thread serverThread = new Thread(new Runnable(){
		@Override
		public void run(){
			while(true){
				try{
					Socket clientSocket = serverSocket.accept();

					RegistryRunnable r = new RegistryRunnable(clientSocket, registry, completed, trafficReport);
					new Thread(r).start();
				} catch (Exception e){
					System.out.println("Unable to accept incoming connection");
				}
			}
		}
	});
	
	public static void main(String[] args) throws IOException{
		System.out.println("[cs455.overlay.node.Registry] Identity: " +  InetAddress.getLocalHost().getHostAddress() + ":" + args[0]);

		boolean mainFlag = true;

		serverSocket = new ServerSocket(new Integer(args[0]));

		links = new ArrayList<String>();
		registry = new HashMap<String, Socket>();
		partners = new HashMap<String, ArrayList<String>>();
		completed = new AtomicInteger(0);

		trafficReport = new TrafficReport();

		serverThread.start();

		Scanner sc = new Scanner(System.in);
		do{
			String command = sc.nextLine();

			switch(command){
				case "list-messaging nodes":
					System.out.println("[cs455.overlay.node.Registry] Size of registry: " + registry.size());
					for(String str: registry.keySet()){
						System.out.println("[cs455.overlay.node.Registry] " + str);
					}
					break;
				case "send-overlay-link-weights":
					linkWeights();
					break;
                case "list-weights":
                    for(String str: links){
                        System.out.println("[cs455.overlay.node.Registry] " + str);
                    }
                    break;
				case "exit":
					mainFlag = false;
					break;
			}

			if(command.contains("setup-overlay")){
				String[] parts = command.split(" ");
				if(parts.length < 2)
					setupOverlay(new Integer(4));
				else
					setupOverlay(new Integer(parts[1]));

			}

			if(command.contains("start")){
			    String[] parts = command.split(" ");

			    int numRounds = new Integer(parts[1]);
			    startMessaging(numRounds);
            }

		} while(mainFlag);
	}

	public static void setupOverlay(int connections){
		if(registry.size() < connections + 1){
			System.out.println("Cannot create overlay with only " + registry.size() + " nodes.");
		}

		String[] list = registry.keySet().toArray(new String[registry.keySet().size()]);

		for(int i = list.length-1; i > 0; i--){
			String intermediate;
			int random = ThreadLocalRandom.current().nextInt(0, i+1);

			intermediate = list[i];
			list[i] = list[random];
			list[random] = intermediate;
		}

		partners = new HashMap<String, ArrayList<String>>();
		for(int i = 0; i < list.length; i++){
			ArrayList<String> temp = new ArrayList<String>();

			for(int j = 0 - (connections/2); j <= 0 + (connections/2); j++){
				if(j == 0)
					continue;

				int partner = (i + j) % list.length;
				if(partner < 0)
					partner = list.length + partner;

				temp.add(list[partner]);
			}
			partners.put(list[i], temp);
		}

		for(String key: list){
			for(String node: partners.get(key)){
				if(partners.get(node).contains(key)){
					partners.get(node).remove(key);
				}
			}
		}

		for(int i = 0; i < list.length; i++){
			NodeListMessage request = new NodeListMessage(partners.get(list[i]).toArray(new String[0]));

			try{
				new DataOutputStream(registry.get(list[i]).getOutputStream()).write(request.getMessage());
			} catch (Exception e){
				System.out.println("Unable to write to output stream. " + e);
			}
		}

		System.out.println("[cs455.overlay.node.Registry] Finished constructing overlay with " + connections + " connections.");
	}

	public static void linkWeights(){
		links = new ArrayList<String>();

		for(String key: partners.keySet()){
			for(String node: partners.get(key)){
				links.add(new String(key + " " + node + " " + ThreadLocalRandom.current().nextInt(1, 11)));
			}
		}

		LinkWeightMessage request = new LinkWeightMessage(links.toArray(new String[0]));

		for(String key: partners.keySet()){
			try{
				new DataOutputStream(registry.get(key).getOutputStream()).write(request.getMessage());
			} catch (Exception e){
				System.out.println("Unable to write to output stream. " + e);
			}
		}

        System.out.println("[cs455.overlay.node.Registry] Finished assigning link weights.");
	}

	public static void startMessaging(int numRounds){
        TaskInitiateMessage request = new TaskInitiateMessage(numRounds);

        try{
            for(String target: registry.keySet()){
                registry.get(target).getOutputStream().write(request.getMessage());
            }
        } catch (Exception e){
            System.out.println("Unable to send initiate message.");
        }

        System.out.println("[cs455.overlay.node.Registry] Starting " + numRounds + " messaging rounds on all nodes.");
    }
}
