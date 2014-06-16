import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * acm
 */
public class ClientBL {


    public static void main(String[] args) {
        new ClientBL();
    }

    public ClientBL() {
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            new Thread() {
                @Override
                public void run() {
                    doRequest(finalI);
                }
            }.start();
        }
    }

    private void doRequest(int i) {
        Socket socket = null;
        String message;
        try {
            socket = new Socket(InetAddress.getByName("localhost"), 9090); // open connection
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            OutputStreamWriter osw = new OutputStreamWriter(bos);
            osw.write("acm:123\r");
            osw.flush();

            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            InputStreamReader isr = new InputStreamReader(bis);
            StringBuilder instr = new StringBuilder();
            int c;
            while ((c = isr.read()) != 13) {
                char ch = (char) c;
                if (c == -1) {
                    break;
                }
                instr.append(ch);
            }
            System.out.println(i + ": response: " + instr.toString().trim());
            socket.close();
        } catch (Exception e) {
            System.out.println("----- [" + i + "] exception e: " + e.getMessage());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
}
