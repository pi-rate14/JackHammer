# JackHammer

JackHammer is a command line tool written in Java to distrbute large scale downloads on different machines.
It utilizes high speed intranet connected to low speed internet.

**Preferably, the application should not be run on a single machine.**

### How to run

1. Connect all the computers through a network. (Wifi Hotspot works)
2. Decide a server(computer) to run the tracker. Run the file server.java on that machine. The tracker runs on the port 8000 on that machine.
3. Enter the ip adress of machine on which tracker runs in **line 100 in server.java** and **line 59 in downloader.java** in the **ClientAPP** folder.
4. Copy this code to all the machines who want to participate in the downloading.
5. Complile all the files in the ClientApp folder and then run the MainApp.
6. Enter the download link in the terminal
7. If some other peers are present, the download will get distributed with them otherwise it will be downloaded completely by the single machine itself.
8. **Close the app after one download.**

### Download on a single machine(Not Recommended)

These are the steps to run this on just a single machine as a downloader.

1. Run the tracker server (server.java) on the machine.
2. Enter the ip adress of machine on which tracker runs in **line 100 in server.java** and **line 59 in downloader.java** in the **ClientAPP** folder.
3. Complile all the files in the ClientApp folder and then run the MainApp.
4. Enter the download link in the terminal
