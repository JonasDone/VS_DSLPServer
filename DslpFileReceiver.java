import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Base64;


public class DslpFileReceiver {

    public static void main(String[] args) {

        if (args.length != 1) {
            throw new IllegalArgumentException();
        }

        String serverAddress = "http://dbl44.beuth-hochschule.de";

        int portInput = 44444;

        SocketClientReceiver client;

        try {
            client = new SocketClientReceiver(serverAddress, portInput);

            client.checkConnection();

            String toFile = client.receiveFile();

            client.stringToFile(toFile, args[0]);

            client.closeAll();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}



class SocketClientReceiver {

    private URL url;
    private InetAddress address;
    private Socket sock;
    private BufferedReader readerSocket;


    public SocketClientReceiver(String input, int port) throws IOException {
        url = new URL(input);
        address = InetAddress.getByName(url.getHost());
        sock = new Socket(address, port);
        readerSocket = new BufferedReader(new InputStreamReader(sock.getInputStream()));

    }


    public void closeAll() throws IOException {

        readerSocket.close();
        sock.close();
    }

    public boolean checkConnection() {
        if (!sock.isConnected()) {
            System.out.println("No Connection to server");
            return false;
        } else {
            System.out.println("Connection to server established. Hostaddress: " + sock.getInetAddress().getHostAddress());
            return true;
        }
    }

    public String receiveFile() {
        String encodedFile = "";
        String line;
        try {
            while (!(line = readerSocket.readLine()).startsWith("dslp/end")) {

                if (line.startsWith("peer")) {
                    String address = readerSocket.readLine();
                    encodedFile = readerSocket.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encodedFile;
    }

    private String decodeFile(String encodedFile) {
        String decoded;

        byte[] bytes = Base64.getDecoder().decode(encodedFile);
        decoded = new String(bytes);

        return decoded;
    }

    public void stringToFile(String encoded, String destinationPath) throws FileNotFoundException {

        String toFile = decodeFile(encoded);

        try (PrintWriter out = new PrintWriter(destinationPath)) {
            out.println(toFile);
        }

    }


}