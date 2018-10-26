import java.io.IOException;

public class BroadcastServer {

    public static void main(String args[])
    {
        try {
            Server s = new Server(14449);
            s.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
