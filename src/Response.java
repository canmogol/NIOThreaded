/**
 * acm
 */
public class Response {

    private byte[] bytes;

    public Response() {
    }

    public Response(byte[] bytes) {
        this.bytes = bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
