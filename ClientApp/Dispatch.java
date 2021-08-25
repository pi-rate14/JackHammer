import java.util.*;
import java.lang.*;
import java.net.*;
import Interfaces.*;
import java.io.*;

//object manages connection with the peer and gets downloaded file from it.

class HandlePeer implements Runnable{
    //port on which service is running on peer
    final int DISPATCH_PORT = 8001;
    //ip of peer
    String ip;
    //URL for download
    URL url;
    //name of file to be downloaded
    String fileName;
    //offset and size managed by this object
    int offset, size, totalRead;
    Thread t;
    Socket s;
    //file to write
    RandomAccessFile f;
    //stream for socket input
    BufferedInputStream is;
    //status of peer
    boolean success;
    //stream for socket output
    PrintWriter os;
    public HandlePeer(String ip, URL url, String fileName, int offset, int size){
        this.t = new Thread(this);
        this.ip = ip;
        this.url = url;
        this.success = false;
        this.fileName = fileName;
        this.offset = offset;
        this.size = size;
        this.totalRead = 0;
        t.start();
    }

    public void run(){
        try{
            s = new Socket(ip, DISPATCH_PORT);
            f = new RandomAccessFile(fileName, "rw");
            is = new BufferedInputStream(s.getInputStream());
            os = new PrintWriter(s.getOutputStream(),true);
            //giving information
            System.out.println("Offset = " + offset + "Size = "+size);
            os.println(url.toExternalForm());
            os.println(offset);
            os.println(size);
            //taking input from socket
            f.seek(offset);

            int rd = 0, left = size, buffSize = 1000;
            byte buff[] = new byte[buffSize];

            do{
                if(left >= buffSize){
                    rd = is.read(buff, 0, buffSize);
                } else {
                    rd = is.read(buff,0,left);
                }
                if(rd != -1){
                    f.write(buff,0,rd);
                    totalRead+=rd;
                    left -= rd;
                } 
                System.out.printf("size: %d totalRead:%d\n", size, totalRead);
            } while (!(left==0 || rd == -1));
            
            if(totalRead == size){
                success = true;
            } else {
                success = false;
            }
        } catch (Exception e){
            System.err.println(e);
        } finally {
            if(!success)
                System.out.println("Peer Failed");
            else
                System.out.println("Peer Success");
            try{
                s.close();
                f.close();
            }
            catch(IOException e){
                System.err.println(e);
            }
        }
    }
}

//object responsible for dispatching file on suitable peers in network

class Dispatch implements Dispatcher {
    //maximum number of nodes on which it can be dispatched
    final int MAX_DISPATCH = 10;
    //url of download
    URL url;
    //Size of file
    int size;
    //filename
    String fileName;
    //set of online peers
    HashSet<String> ipList;

    public Dispatch(HashSet<String> ipList, URL url, int size, String fileName){
        System.out.println("Dispatch constructir activated");
        this.ipList = ipList;
        this.url = url;
        this.size = size;
        this.fileName = fileName;
    }

    //function to distribute file across online peers
    public boolean Distribute(){
        System.out.println("Distributing");

        int count = 0;
        int offset = 0;
        int chunk = (size/ipList.size() +1);
        System.out.println("size = " + size + " offset = "+ offset + " chunk = " + chunk);
		HandlePeer peers[] = new HandlePeer[MAX_DISPATCH];

        for(String ip: ipList){
            System.out.println("Dispatching to: "+ip+" offset: "+offset);
            if(size - offset < chunk){
                peers[count++] = new HandlePeer(ip, url, fileName, offset, size-offset);
            } else {
                peers[count++] = new HandlePeer(ip, url, fileName, offset, chunk);
            }
            offset += chunk;
        }
        try{
            for(int i=0; i<count; i++){
                peers[i].t.join();
            }
        } catch (InterruptedException e){
            System.err.println(e);
        }
        return true;
    }
}