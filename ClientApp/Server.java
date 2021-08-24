import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

//This class handles downloads for connected peers and diverts traffic
class PeerHandler implements Runnable{

    Thread t;
    Socket s;
    BufferedReader is;
    BufferedOutputStream os;
    int totalRead, size, offset;
    String url;
    boolean success;
    final int buffSize = 1000;
    public PeerHandler(Socket cl){
        t = new Thread(this);
        s = cl;
        totalRead = 0;
        success = false;
        try{
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new BufferedOutputStream(s.getOutputStream());
            t.start();
        } catch(IOException e){
            System.err.println(e);
            try{
                s.close();
            } catch(IOException ex){
                System.err.println(ex);
            }
        }
    }

    public void run(){
        //getting info from peer
        System.out.println("Getting info from peer");
        try{
            url = is.readLine();
            offset = Integer.parseInt(is.readLine());
            size = Integer.parseInt(is.readLine());

            System.out.println("Downloading from: ");
            System.out.println("URL: "+url);
            System.out.println("offset: "+offset);
            System.out.println("size: "+size);

            //starting the download
            URL u = new URL(url);
            URLConnection uc = u.openConnection();
            //setting offset location
            uc.setRequestProperty("Range", "bytes = " + offset + "-");
            InputStream dis = uc.getInputStream();
            byte buff[] = new byte[buffSize];

            int left = size, rd=0;
            do {
                if(left <= buffSize){
                    rd = dis.read(buff, 0, left);
                } else {
                    rd = dis.read(buff, 0, buffSize);
                }
                if(rd != -1){
                    os.write(buff, 0, rd);
                    os.flush();
                    totalRead += rd;
                    left -= rd;
                }
                System.out.printf("size: %d totalRead:%d\n", size, totalRead);
            } while(!(left==0 || rd == -1));

            if(totalRead == size){
                success = true;
            } else {
                success = false;
            }
        } catch(IOException e){
            System.err.println(e);
        } finally {
            try {
                s.close();
                System.out.println("Closing the socket");
            } catch(IOException e){
                System.err.println(e);
            }
        }
    }

}

//This class runs the main server to provide resource sharing to peers
class Server implements Runnable{
    
    //port on which service runs
    final int PEER_SERVER_PORT = 8001;
    //port on which tracker runs
    final int TRACKER_PORT = 8000;
    //ip adddress of the tracker
    final String TRACKER_IP = "192.168.0.112";
    ServerSocket s;
    Socket tracker;
    //creating stream for tracker input
    BufferedReader is;
    //creating stream for tracker output
    PrintWriter os;
    Thread t;
    public Server(){

        t = new Thread(this);
        try{
            s = new ServerSocket(PEER_SERVER_PORT);
            //registering service on tracker
            tracker = new Socket(TRACKER_IP, TRACKER_PORT);
            is = new BufferedReader(new InputStreamReader(tracker.getInputStream()));
            os = new PrintWriter(tracker.getOutputStream(),true);
            os.println("Server");
            t.start();
        } catch(IOException e){
            System.err.println(e);
        }

    }

    public void run(){
        System.out.println("The peer server is running on port "+ PEER_SERVER_PORT);
        while(true){
            try{
                System.out.println("Waiting to accept");
                Socket cl = s.accept();
                System.out.println("yay! got a peer!");
                new PeerHandler(cl);
            } catch(IOException e){
                System.err.println(e);
            }
        }
    }


}