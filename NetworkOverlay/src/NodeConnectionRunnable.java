import wireformats.*;
import wireformats.Message.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeConnectionRunnable implements Runnable{
    public Socket nodeSocket;
    public DataOutputStream dos;
    public DataInputStream dis;
    public HashMap<String, Socket> connections;

    byte[] fragment = null;

    public AtomicInteger sendTracker;
    public AtomicInteger relayTracker;
    public AtomicInteger receiveTracker;

    public NodeConnectionRunnable(Socket _clientSocket, HashMap<String, Socket> _connections, AtomicInteger _sendTracker, AtomicInteger _receiveTracker, AtomicInteger _relayTracker){
        nodeSocket = _clientSocket;
        connections = _connections;
        sendTracker = _sendTracker;
        relayTracker = _relayTracker;
        receiveTracker = _receiveTracker;

        try {
            dos = new DataOutputStream(nodeSocket.getOutputStream());
            dis = new DataInputStream(nodeSocket.getInputStream());
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
            System.out.println("Node Connection Runnable - Unable to get available bytes. " + e);
        }

        System.out.println("Done");
    }

    public void sanitizeMessages(byte[] bytestring) throws IOException{
        if(fragment != null){
            byte[] combine = new byte[bytestring.length + fragment.length];
            System.arraycopy(fragment, 0, combine, 0, fragment.length);
            System.arraycopy(bytestring, 0, combine, fragment.length, bytestring.length);

            bytestring = combine;
            fragment = null;
        }

        while(bytestring.length > 0){
            if(bytestring.length < 4){
                fragment = bytestring;
                break;
            }

            ByteBuffer buffer = ByteBuffer.wrap(bytestring);
            int length = buffer.getInt();

            if(length <= bytestring.length){
                byte[] message = Arrays.copyOfRange(bytestring, 4, length);

                handleMessage(message);

                bytestring = Arrays.copyOfRange(bytestring, length, bytestring.length);
            } else {
                fragment = bytestring;
                break;
            }
        }
    }

    public void handleMessage(byte[] message) throws IOException{
        ByteBuffer buffer = ByteBuffer.wrap(message);
        MessageType type = MessageType.values()[buffer.getInt()];

        switch(type){
            case REGISTER_REQUEST:
                register(buffer.getInt(), new String(Arrays.copyOfRange(message, 8, message.length)));
                break;
            case TEST_MESSAGE:
                System.out.println("Test data recieved.");
                break;
            case DATA_TRANSMISSION:
                handleTransmission(message);
        }
    }

    public void handleTransmission(byte[] message) throws IOException{
        ByteBuffer buffer = ByteBuffer.wrap(message);

        int payload = buffer.getInt(4);
        String path = new String(Arrays.copyOfRange(message, 8, message.length));

        if(path.length() > 0){
            String[] parts = path.split(" ");
            String target = parts[0];

            path = "";
            for(int i = 1; i < parts.length; i++){
                path += parts[i];
            }

            //System.out.println("Passing data...");
            sendTracker.getAndIncrement();
            DataTransmissionMessage request = new DataTransmissionMessage(path, payload);
            if(connections.containsKey(target))
                connections.get(target).getOutputStream().write(request.getMessage());
        } else {
            receiveTracker.getAndIncrement();
        }
    }

    public void register(int _port, String _host){
        System.out.println("[Node] Connection to " + _host + ":" + _port);
        connections.put(_host + ":" + _port, nodeSocket);
    }
}
