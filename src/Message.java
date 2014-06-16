import java.nio.channels.SocketChannel;

/**
 * acm
 */
public class Message {

    private MessageEvent messageEvent;
    private MessageHandler handler;
    private SocketChannel socketChannel;
    private byte[] data;
    private int numberOfBytes;

    public Message(MessageEvent messageEvent, MessageHandler handler, SocketChannel socketChannel, byte[] data, int numberOfBytes) {
        this.messageEvent = messageEvent;
        this.handler = handler;
        this.socketChannel = socketChannel;
        this.data = data;
        this.numberOfBytes = numberOfBytes;
    }

    public Message(MessageEvent messageEvent, MessageHandler handler, SocketChannel socketChannel) {
        this(messageEvent, handler, socketChannel, new byte[0], 0);
    }

    public MessageHandler getHandler() {
        return handler;
    }

    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getNumberOfBytes() {
        return numberOfBytes;
    }

    public void setNumberOfBytes(int numberOfBytes) {
        this.numberOfBytes = numberOfBytes;
    }

    public MessageEvent getMessageEvent() {
        return messageEvent;
    }

    public void setMessageEvent(MessageEvent messageEvent) {
        this.messageEvent = messageEvent;
    }
}
