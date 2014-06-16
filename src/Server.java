import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server implements MessageHandler, Runnable {

    // The selector we'll be monitoring
    private Selector selector;
    private boolean running = true;

    // The buffer into which we'll read data when it's available
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

    private MessageHandler applicationHandler;

    // A list of PendingChange instances
    private final List<ChangeRequest> pendingChanges = new LinkedList<>();

    // Maps a SocketChannel to a list of ByteBuffer instances
    private final Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<>();

    public Server(MessageHandler applicationHandler) throws IOException {
        selector = Selector.open();
        this.applicationHandler = applicationHandler;
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(9090);
        ServerSocket ss = ssc.socket();
        ss.bind(address);
        SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void stop() {
        running = false;
        this.selector.wakeup();
    }

    public void send(Message message) {
        synchronized (this.pendingChanges) {
            // Indicate we want the interest ops set changed
            this.pendingChanges.add(new ChangeRequest(message.getSocketChannel(), ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

            // And queue the data we want written
            synchronized (this.pendingData) {
                List<ByteBuffer> queue = this.pendingData.get(message.getSocketChannel());
                if (queue == null) {
                    queue = new ArrayList<>();
                    this.pendingData.put(message.getSocketChannel(), queue);
                }
                queue.add(ByteBuffer.wrap(message.getData()));
            }
        }

        // Finally, wake up our selecting thread so it can make the required changes
        this.selector.wakeup();
    }

    public void run() {
        while (running) {
            try {
                // Process any pending changes
                synchronized (this.pendingChanges) {
                    for (ChangeRequest change : this.pendingChanges) {
                        switch (change.type) {
                            case ChangeRequest.CHANGEOPS:
                                SelectionKey key = change.socket.keyFor(this.selector);
                                key.interestOps(change.ops);
                        }
                    }
                    this.pendingChanges.clear();
                }

                // Wait for an event one of the registered channels
                this.selector.select();

                // Iterate over the set of keys for which events are available
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();
                    if (!selectionKey.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (selectionKey.isAcceptable()) {
                        this.accept(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        this.read(selectionKey);
                    } else if (selectionKey.isWritable()) {
                        this.write(selectionKey);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(this.selector, SelectionKey.OP_READ);

        applicationHandler.send(new Message(MessageEvent.ACCEPT, this, socketChannel));
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        this.readBuffer.clear();

        // Attempt to read off the channel
        int numberOfBytes;
        try {
            numberOfBytes = socketChannel.read(this.readBuffer);
            if (numberOfBytes == 0) {
                applicationHandler.send(new Message(MessageEvent.EMPTY_READ, this, socketChannel));
            } else if (numberOfBytes < 0) {
                // Remote entity shut the socket down cleanly. Do the
                // same from our end and cancel the channel.
                throw new Exception("connection closed");
            } else {
                // Hand the data off to our applicationHandler thread
                applicationHandler.send(new Message(MessageEvent.READ, this, socketChannel, this.readBuffer.array(), numberOfBytes));
            }
        } catch (Exception e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            applicationHandler.send(new Message(MessageEvent.CLOSE, this, socketChannel));
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (this.pendingData) {
            List queue = (List) this.pendingData.get(socketChannel);

            // Write until there's not more data ...
            while (!queue.isEmpty()) {
                ByteBuffer buf = (ByteBuffer) queue.get(0);
                socketChannel.write(buf);
                socketChannel.write(ByteBuffer.wrap("\n\r\n\r".getBytes()));
                if (buf.remaining() > 0) {
                    // ... or the socket's buffer fills up
                    break;
                }
                queue.remove(0);
            }
            applicationHandler.send(new Message(MessageEvent.WRITE, this, socketChannel));

            if (queue.isEmpty()) {
                // We wrote away all data, so we're no longer interested
                // in writing on this socket. Switch back to waiting for
                // data.
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    public static void main(String[] args) {
        try {
            ApplicationHandler applicationHandler = new ApplicationHandler();
            new Thread(applicationHandler).start();
            Server server = new Server(applicationHandler);
            new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
