import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.*;

/**
 * Created by Rakesh on 11/14/2015.
 */
public class DownloadManager extends Thread {
    String Name = null, Size = null, Pieces = null, Tracker = null, peerId = null;
    Socket downloadManagerSocket;
    int serverSocketPort = 8090;
    String downloadPath = "D:\\xampp\\htdocs\\Bittorrent\\Files\\";
    int retPieceLength =0;


    public DownloadManager(String name, String tracker, String size, String pieces, String peerId) {
        this.Name = name;
        this.Tracker = tracker;
        this.Size = size;
        this.Pieces = pieces;
        this.peerId = peerId;
    }

    public void run() {
        //create a tracker request using the tracker url
        try {
            //establish a socket connection with the tracker
            downloadManagerSocket = new Socket(Tracker, serverSocketPort);
            sendTrackerRequest();
        } catch (Exception se) {
            System.out.println(se.toString());
        }
    }

    public void sendTrackerRequest() throws IOException {
        //get sockets output stream
        PrintWriter printWriter = new PrintWriter(downloadManagerSocket.getOutputStream());
        System.out.println("requesting" + "," + "File Name:" + Name + "," + "PeerId:" + peerId);
        printWriter.println("requesting" + "," + "File Name:" + Name + "," + "PeerId:" + peerId);
        printWriter.flush();
        readTrackerResponse();
    }

    public void readTrackerResponse() throws IOException {
        ObjectInputStream peerDetailsInputStream = new ObjectInputStream(downloadManagerSocket.getInputStream());
        ArrayList<String> peerIds = new ArrayList<String>();
        ArrayList<ArrayList<String>> peerDetails = new ArrayList<ArrayList<String>>();
        try {
            HashMap<String, AbstractList<String>> peers = (HashMap<String, AbstractList<String>>) peerDetailsInputStream.readObject();
            Iterator it = peers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                peerIds.add(String.valueOf(pair.getKey()));
                peerDetails.add((ArrayList<String>) pair.getValue());
//                System.out.println(pair.getKey() + " = " + pair.getValue());
//                it.remove(); // avoids a ConcurrentModificationException
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //for each peer invoke download
//        int pieceLength=0;
        for (int i = 0; i < peerIds.size(); i++) {
//            System.out.println(i);
            retPieceLength += downloadStrategy(String.valueOf(peerDetails.get(i).get(0)), Integer.parseInt(String.valueOf(peerDetails.get(i).get(2))));
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String directory = "C:\\Users\\Rakesh\\IdeaProjects\\Peer1\\TempDownloads\\" + FilenameUtils.getBaseName(Name);
        int noOfPieces = getNumberParts(directory + "\\" + Name);
        System.out.println("no of pieces" + noOfPieces);
        System.out.println(retPieceLength);
        if (noOfPieces == retPieceLength) {
            // write the downloaded info to tracker xml
            System.out.println("Updating XMl in the tracker to indicate this peer also has the file");
            PrintWriter printWriter = new PrintWriter(downloadManagerSocket.getOutputStream());
            String tempIP = InetAddress.getLocalHost() +":81";
            printWriter.write("updating" + "," + Name + "," + peerId + "," + tempIP+ "," + 81 + "," + noOfPieces);
            printWriter.flush();
            //create a new info.ase file inside the downloads folder after download
            PrintWriter writer = new PrintWriter(downloadPath+ FilenameUtils.getBaseName(Name) + "//"+"info.ase","UTF-8");
            for(int i=0;i<noOfPieces;i++) {
                writer.print(i + "\t");
            }
            writer.close();
            joinFiles();
        }
    }

    public int downloadStrategy(String ip, int pieceLength) throws IOException {
        try {
            retPieceLength =0;
            String[] temp = ip.split("/");
            String newIp = temp[1];
            String BaseURL = "http://" + newIp + "/Bittorrent/Files/" + FilenameUtils.getBaseName(Name) + "/" + Name;
            System.out.println(BaseURL);
            String[] temp1 = newIp.split(":");
            String pingIp = temp1[0];
            //get the piece info file
            String pieceInfo = "http://" + newIp + "/Bittorrent/Files/" + FilenameUtils.getBaseName(Name) + "/" + "info.ase";
            URL pieceInfoURL = new URL(pieceInfo);
            BufferedReader br = new BufferedReader(new InputStreamReader(pieceInfoURL.openStream()));
            String inputLine;
            String[] piecesToDownload = new String[pieceLength];
            while ((inputLine = br.readLine()) != null) {
                System.out.println("pieces to download"+ inputLine);
                piecesToDownload = inputLine.split("\t");
            }
            br.close();
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -n 1 " + pingIp);
            int returnVal = 1;
            try {
                returnVal = p1.waitFor();
            } catch (InterruptedException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            boolean reachable = (returnVal == 0);
            String checkDownloadPath = downloadPath + FilenameUtils.getBaseName(Name) + "//"+ Name;
            if (reachable) {
                try {
                    for (int i = 0; i < pieceLength; i++) {
                        // check if the piece is already downloaded
                        File downloadedPiece = new File(checkDownloadPath + "." + i);
                        boolean exists = downloadedPiece.exists();
                        if(!exists) {
                            retPieceLength++;
                            Thread thread = new Thread(new DownloadPiece(BaseURL + "." + piecesToDownload[i], downloadPath + FilenameUtils.getBaseName(Name) + "//"));
                            thread.start();
                        }else{
                            System.out.println("Piece"+ i +"already downloaded... downloading other pieces");
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return retPieceLength;
    }

    public void joinFiles() {
        try {
            FileSplitter.join(Name);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static int getNumberParts(String baseFilename) throws IOException {
        // list all files in the same directory
        File directory = new File(baseFilename).getAbsoluteFile().getParentFile();
        final String justFilename = new File(baseFilename).getName();
        String[] matchingFiles = directory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(justFilename) && name.substring(justFilename.length()).matches("^\\.\\d+$");
            }
        });
        return matchingFiles.length;
    }
}
