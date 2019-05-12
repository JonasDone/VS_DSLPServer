import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class ClientThreads implements Runnable {

    Socket s;
    BufferedReader reader;
    BufferedWriter writer;
    String group;
    String name;

    public ClientThreads(Socket s, BufferedReader reader, BufferedWriter writer, String name) {
        this.s = s;
        this.reader = reader;
        this.writer = writer;
        this.group = "";
        this.name = name;
    }

    @Override
    public void run() {

        try {
            checkForPeerMsg();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String received = "";
        ArrayList<String> list = new ArrayList<>();

        try {
            while ((received = reader.readLine()) != null) {

                if (!received.startsWith("dslp/end")) {
                    list.add(received);

                } else {

                    writer.write("dslp/1.2\r\n");

                    switch (list.get(1)) {

                        case "request time": //works
                            handleRequestTime(list, writer);
                            break;

                        case "group join": //works
                            handleGroupJoin(list, writer);
                            break;

                        case "group leave": //works
                            handleGroupLeave(list, writer);
                            break;

                        case "group notify": //works
                            handleGroupNotify(list, writer);
                            break;

                        case "peer notify": //works
                            handlePeerNotify(list, writer);
                            break;

                        default: //works
                            handleError(list, writer);
                            break;
                    }
                    list.clear();

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Server.threadsVector.remove(this);


        try {
            this.reader.close();
            this.writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Server.i--;
        System.out.println(name + " disconnected");

        //return;
    }

    private void handleRequestTime(ArrayList<String> list, BufferedWriter writer) throws IOException {
        if (list.get(0).startsWith("dslp/1.2") && list.size() == 2) {

            Date now = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            String formattedTime = format.format(now);

            writer.write("response time\r\n");
            writer.write(formattedTime + "\r\n");
            writer.write("dslp/end\r\n");
            writer.flush();
        } else {
            handleError(list, writer);
        }
    }


    private void handleGroupJoin(ArrayList<String> list, BufferedWriter writer) throws IOException {
        if (list.get(0).startsWith("dslp/1.2") && list.size() == 3 && !list.get(2).trim().isEmpty())
            this.group = list.get(2);
        else handleError(list, writer);

    }

    private void handleGroupLeave(ArrayList<String> list, BufferedWriter writer) throws IOException {
        if (list.get(0).startsWith("dslp/1.2") && list.size() == 3) {
            if (group.equals(list.get(2))) group = "";
            else {
                writer.write("Not memeber of this group\r\n");
                writer.flush();
            }
        } else {
            handleError(list, writer);
        }

    }

    private void handleGroupNotify(ArrayList<String> list, BufferedWriter writer) throws IOException {
        if (list.get(0).startsWith("dslp/1.2")) {
            for (ClientThreads ct : Server.threadsVector) {
                if (ct.group.equals(list.get(2))) {
                    for (int i = 3; i < list.size(); i++) {
                        ct.writer.write(list.get(i) + "\r\n");
                    }
                    ct.writer.write("dslp/end\r\n");
                    ct.writer.flush();
                }
            }
        } else {
            handleError(list, writer);
        }

    }

    private void handlePeerNotify(ArrayList<String> list, BufferedWriter writer) throws IOException {

        boolean msgDelivered = false;

        if (list.get(0).startsWith("dslp/1.2")) {
            for (ClientThreads ct : Server.threadsVector) {
                String peerAddress = ct.s.getInetAddress().toString().substring(1);

                if (peerAddress.equals(list.get(2))) {
                    for (String msg : list) {
                        ct.writer.write(msg);
                    }
                    ct.writer.write("dslp/end\r\n");
                    ct.writer.flush();

                    msgDelivered = true;
                }
            }
        } else {
            handleError(list, writer);
        }


        if (msgDelivered == false) {

            LinkedList<String> msgList = new LinkedList<>();

            for (String msg : list) {
                msgList.add(msg);
            }
            Server.msgVector.add(msgList);
        }
    }

    private void handleError(ArrayList<String> list, BufferedWriter writer) throws IOException {

        writer.write("error\r\n");
        if (list.size() > 1)
            if (list.get(0).startsWith("dslp/1.2"))
                writer.write(String.format("I don't know what the message type %s means.\r\n", list.get(1)));
            else writer.write(String.format("Unkown Server %s", list.get(0)));
        writer.write("Please consult the DSLP 1.2 specification for a list of valid message types.\r\n");
        writer.write("dslp/end\r\n");
        writer.flush();
    }


    public void checkForPeerMsg() throws IOException {
        for (LinkedList<String> list : Server.msgVector) {

            String peerAddress = this.s.getInetAddress().toString().substring(1);

            if (list.get(2).equals(peerAddress)) {
                for (String msg : list) {
                    writer.write(msg + "\r\n");
                }
                writer.write("dslp/end\r\n");
                writer.flush();
                Server.msgVector.remove(list);
            }
        }
    }

}
