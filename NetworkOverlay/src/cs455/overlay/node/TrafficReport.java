package cs455.overlay.node;

public class TrafficReport {
    private long sumSent;
    private long sumReceived;
    private int sent;
    private int received;

    private int contributers;
    private boolean header;

    public TrafficReport(){
        sent = 0;
        received = 0;
        sumSent = 0;
        sumReceived = 0;

        contributers = 0;
        header = false;
    }

    public synchronized void contribute(int _sent, long _sumSent, int _received, long _sumReceived){
        sent += _sent;
        sumSent += _sumSent;
        received += _received;
        sumReceived += _sumReceived;

        contributers++;
    }

    public synchronized boolean headerBuilt(){
        if(!header){
            header = true;
            return false;
        }

        return header;
    }

    public synchronized int getContributers(){
        return contributers;
    }

    public String toString(){
        String output = "|          Sum          ";
        String temp = "";

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

        output += "  |";
        return output;
    }
}
