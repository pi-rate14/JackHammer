import java.util.*;

import jdk.internal.org.jline.utils.InputStreamReader;

import java.net.*;
import java.io.*;

//tracker object is responsible for saving information of the peers providing the download service

class Tracker{
    //set of peers currently online
    HashSet<String> ipList;
    public Tracker(){
        ipList = new HashSet<String>();
    }

    //add the ip to the list of peers
    synchronized public void addIP(String ip){
        System.out.println("Adding ip: "+ip);
        ipList.add(ip);
    }

    //give information of online peers to output stream
    synchronized public void refresh(PrintWriter os){
        for(String s: ipList){
            System.out.println("Written: "+s);
            os.println(s);
        }
        System.out.println("Written .");
        os.println(".");
    }

    //Deletes IP from set of online peers
    synchronized public void deleteIP(String ip){
        if(ipList.contains(ip)){
            ipList.remove(ip);
        }
    }
}

//This object handles the connection between a peer server or peer client
class Handler implements Runnable{
    Thread t;
    Socket s;
    //Stream for socket input
    BufferedReader is;
    //Stream for socket output
    PrintWriter os;
    //Tracker object to store information
    Tracker tracker;
    //IP of connected peer
    String clientIP;

    public Handler(Socket s, Tracker tracker){
        try{
            t = new Thread(this);
            this.s = s;
            this.tracker = tracker;
            //adding client to tracker list
            clientIP = s.getInetAddress().getHostAddress();
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream(),true);
        } catch(Exception e){
            System.out.println(e);
        }
        t.start();
    }

    public void run(){
        //accepting requessts
        try{
            while(!s.isInputShutdown()){
                System.out.println("In Thread");
                String request = is.readLine();

                System.out.println("request: "+request);
                //identifying peer as server or client
                if(request.equals("Server"))
                    tracker.add(clientIP);
                //request to get list of peers
                else if(request.equals("refresh"))
                    tracker.refresh(os);
                //request to disconnect the peer
                else if(request.equals("disconnect"))
                    break;
                else throw new Exception("Invalid request made");
            }
        } catch(Exception e){
            System.err.println(e);
        } finally {
            try{
                is.close();
                os.close();
                s.close();
            } catch(Exception e){
                System.err.println(e);
            }
            finally{
                tracker.deleteIP(clientIP);
                System.out.println(clientIp + " disconnected");
            }
        }
        return;
    }
}

//main server class that manages the tracker server

class Server {
    final static int TRACKER_PORT = 8000;
    public static void main(String args[]){
        try (ServerSocket server = new ServerSocket(TRACKER_PORT)){
            System.out.println("The tracker server is running on port " + TRACKER_PORT);

            //accepting connection to take info 
            //connecting with new user and saving its information in the tracker
            //timely ping clients to see if someone went offline

            Tracker tracker = new Tracker();
            while(true){
                Socket client = server.accept();
                new Handler(client,tracker);
            }
        } catch(Exception e){
            System.err.println("Socket failed to open");
        }
    }
}