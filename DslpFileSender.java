
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class DslpFileSender {

    public static void main(String[] args){
        if(args.length != 2) throw new IllegalArgumentException();

        String serverAddress = "http://dbl44.beuth-hochschule.de";

        int portInput = 44444;

        SocketClientSender client = null;

        try {
            client = new SocketClientSender(serverAddress, portInput);

            client.checkConnection();

            client.sendFile(args[0], args[1]);


        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            client.closeAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class SocketClientSender {

    private URL url;
    private InetAddress address;
    private Socket sock;
    private PrintWriter writerSocket;


    public SocketClientSender(String input, int port) throws IOException {
        url = new URL(input);
        address = InetAddress.getByName(url.getHost());
        sock = new Socket(address, port);
        writerSocket = new PrintWriter(sock.getOutputStream());
    }

    public void sendFile(String ip, String fileName) throws IOException{
        System.out.println("Request: Send " + fileName + " to " + ip);

        writerSocket.write("dslp/1.2\r\n");
        writerSocket.write("peer notify\r\n");
        writerSocket.write(ip + "\r\n");
        writerSocket.write(encodeFile(fileName) + "\r\n");
        writerSocket.write("dslp/end\r\n");

        writerSocket.flush();
    }

    public void closeAll() throws IOException {
        writerSocket.close();
        sock.close();
    }

    public boolean checkConnection(){
        if(!sock.isConnected()){
            System.out.println("No Connection to server");
            return false;
        }else{
            System.out.println("Connection to server established. Hostaddress: " + sock.getInetAddress().getHostAddress());
            return true;
        }
    }

    private String encodeFile(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        String encoded = Base64.getEncoder().encodeToString(bytes);
        return encoded;
    }

}