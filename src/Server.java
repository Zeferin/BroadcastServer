import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class Server{

    private int port;

    public Server(int port)
    {
        this.port = port;
    }

    public void start() throws IOException {
        // the selector is the one who listen for any incoming message
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        // retrieve server socket and bind to port
        serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
        // the selector agrees to listen on serverSocket for any incoming message
        // we will interact with multiple sockets. Please, remember that this is the only socket
        // that we have so far. Its single purpose is to accept new clients (new connections)
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);


        while(true)
        {
            // wait here for any incoming message
            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            System.out.println(selectedKeys.size());
            // check what kind of messages have been received
            for(SelectionKey key:selectedKeys)
            {
                System.out.println(key.isWritable() +","+key.isReadable()+","+key.isAcceptable());
                if (key.isAcceptable()) {
                    // new client is trying to connect
                    acceptNewClient(selector, serverSocket);
                }

                if (key.isReadable()) {
                    // an existing client is sending a message
                    String m = getMessage(key);
                    broadcastMessage(m, selector);
                }
            }
            selectedKeys.clear();
        }
    }


    private void acceptNewClient(Selector selector, ServerSocketChannel serverSocket)
            throws IOException {

        // a fresh new socket is created when accepting the client
        SocketChannel clientSocket = serverSocket.accept();
        // non blocking communication
        clientSocket.configureBlocking(false);
        // the selector agrees to listen on serverSocket for any incoming message
        // please, notice that this is the place where new sockets are being created
        // their purpose is to receive messages from the clients
        clientSocket.register(selector, SelectionKey.OP_READ);
    }

    private String getMessage(SelectionKey key)
            {

        // the place where the incoming message is stored
        ByteBuffer buffer = ByteBuffer.allocate(256);
        SocketChannel client = (SocketChannel) key.channel();

        try {
            client.read(buffer);
        }catch(IOException e)
        {
            key.cancel();
        }
      /*  if (new String(buffer.array()).trim().equals(POISON_PILL)) {
            client.close();
            System.out.println("Not accepting client messages anymore");
        }*/
        return new String(buffer.array());
    }

    private void broadcastMessage(String m, Selector selector) throws IOException {
        Set<SelectionKey> allKeys = selector.keys();

        for(SelectionKey key:allKeys)
        {
            if(key.isValid() && key.isAcceptable() == false) {
                System.out.println(key);
                SocketChannel client = (SocketChannel) key.channel();
                client.write(ByteBuffer.wrap(m.getBytes()));
            }
        }
    }
}
