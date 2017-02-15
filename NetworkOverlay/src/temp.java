import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Chase on 2/14/2017.
 */
public class temp {
    public static void main(String[] args){
        for(int i = 0; i < 10; i++){
            System.out.println(ThreadLocalRandom.current().nextInt());
        }
    }
}
