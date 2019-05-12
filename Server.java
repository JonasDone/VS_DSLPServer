import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Vector;

public class Server {

    //Vector for storage of active clients
    static Vector<ClientThreads> threadsVector = new Vector<>();

    //Vector for stored peer messages
    static Vector<LinkedList> msgVector = new Vector<>();

    //counter for client naming
    static int i = 0;

    public static void main(String[] args) throws IOException {

        ServerSocket ssock = new ServerSocket(44444);

        Socket s;

        try {

            while (true){
                s = ssock.accept();

                System.out.println("New Client connected: Client " + i);

                BufferedReader reader =new BufferedReader(new InputStreamReader(s.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                ClientThreads cli = new ClientThreads(s, reader, writer, "Client " + i);

                Thread t = new Thread(cli);

                threadsVector.add(cli);

                t.start();

                i++;
            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }

}
