
public class TrafficReport {
    private long sumSent;
    private long sumReceived;
    private int sent;
    private int received;

    private int contributers;

    public TrafficReport(){
        sent = 0;
        received = 0;
        sumSent = 0;
        sumReceived = 0;

        contributers = 0;
    }

    public synchronized void contribute(int _sent, long _sumSent, int _received, long _sumReceived){
        sent += _sent;
        sumSent += _sumSent;
        received += _received;
        sumReceived += _sumReceived;

        contributers++;
    }

    public synchronized int getContributers(){
        return contributers;
    }

    public String toString(){
        return ("Total: Sent: " + sent + " Sum: " + sumSent + " Received: " + received + " Sum: " + sumReceived);
    }
}
