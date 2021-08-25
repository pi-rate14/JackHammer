import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;
import INTF.*;

//thread responsible to download file from offset and writing to file

class Downloader implements Runnable{
    
    Thread t;
    String fileName;
    int offset, CHUNK;
    URLConnection uc;

    public Downloader(String fileName, int offset, int CHUNK, URLConnection uc){
        this.t = new Thread(this);
        this.fileName = fileName;
        this.offset = offset;
		this.uc = uc;
		this.CHUNK = CHUNK;
		this.t.start();
    }

    public void run(){

        try (RandomAccessFile f = new RandomAccessFile(fileName,"rw")) {
            uc.setRequestProperty("Range", "Bytes = "+ offset+"-");
            System.out.println("offset =" + offset);
            //going to the position in the file
            f.seek(offset);
            InputStream is = uc.getInputStream();
            int left = CHUNK, ind =0;
            byte buff[] = new byte[CHUNK];
			int rd,dwn = 0;
			do {
				rd = is.read(buff,ind,left);
				if(rd != -1 && left > 0){
				dwn += rd;
				ind += rd;
				left -= rd;
			}
			}
			while(rd != -1 && left > 0);
			f.write(buff,0,dwn);
			System.out.println("Downloaded " + dwn);
        } catch(Exception e){
            System.err.println(e);
        }
    }
}

//client rhread managing download of a particular file

class ClientA implements Runnable, Client{
    Thread t;
    final int TCOUNT = 4;
    final int TRACKER_PORT = 8000;
    final String TRACKER_IP = "192.168.0.112";
    final int NODE_CHUNK = 1000000;
    Socket tracker;
    URL url;
    int size;
    String fileName;
    BufferedReader is;
    PrintWriter os;
    HashSet<String> ipList;
    public ClientA(String url){
        t = new Thread(this);
        try{
            tracker = new Socket(TRACKER_IP, TRACKER_PORT);
            is = new BufferedReader(new InputStreamReader(tracker.getInputStream()));
            os = new PrintWriter(tracker.getOutputStream(), true);
            ipList = new HashSet<String>();
            this.url = new URL(url);
            URLConnection connection = this.url.openConnection();
            size = connection.getContentLength();
            System.out.println("Size of File: "+size);
            fileName = getFileName(url);
            t.start();
        }
        catch(MalformedURLException e){
            System.err.println(e);
        }
        catch(IOException ex){
            System.err.println(ex);
        }
    }

    //function to get list of online peers from tracker
    public boolean checkPeers(){
        System.out.println("Checking peer availability");
        System.out.println("Connected to" +tracker);
        try{
            //requesting clients
            os.println("refresh");
            while(true){
                String ip = is.readLine();
                System.out.println("read "+ip);
                if(ip.equals("."))
                    break;
                else if(!ipList.contains(ip))
                    ipList.add(ip);
            }
        } catch(Exception e) {
            System.err.println(e);
        } finally{
            System.out.println("IP List Size: "+ipList.size());
            return ipList.size() > 1 ;
        }
    }

    /*
		Set up all the parameters
			Check for avilable peers
				Dispatch the files using (Dispatch) object
				Contact the peers with file in new object (Handler) object for each peer
				Manage all file parts that are successfully downloaded in Handler
				For broken files manage downloading those broken parts in other object (ManageBroken)
			If No peer, Download as it is in Download function
	*/	
    public void run(){
        System.out.println("Running in thread");
        try{
            if(!checkPeers()){
                System.err.println("No peer found, downloading on local machine");
                donwload();
                return;
            }
        } finally {
            try{
                tracker.close();
            } catch (IOException e){
                System.err.println(e);
            }
        }
    }

    //function to download file on local machine
    public void download() {
        System.out.println("Opening connection and starting download");
        Downloader d[] = new Downloader[TCOUNT];
        int chunkSize = this.size/TCOUNT+1;
        int offset = 0, ind = 0;
        //downloading file parallely on local machine using TCOUNT threads
        while(offset < size){
            try{
                d[ind++] = new Downloader(this.fileName, offset, chunkSize, this.url.openConnection());
                offset+=chunkSize;
            } catch(Exception e){
                System.err.println(e);
            }
        }
        //waiting for threads to finish downloading
        try{
            for(int i=0;i<ind;i++){
                d[i].t.join();
            }
        }
        catch (InterruptedException e) {
			System.out.println(e);
		}
    }

    public String getFileName(String url) {
		System.out.println("get name");
		return new String("newfile");

	}
}