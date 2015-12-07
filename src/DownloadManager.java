import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.sort;

/**
 * Created by Rakesh on 11/14/2015.
 */
public class DownloadManager extends Thread {
    String Name = null, Size = null, Tracker = null, peerId = null;
    int Pieces;
    Socket downloadManagerSocket;
    int serverSocketPort = 8090;
    String downloadPath = "D:\\xampp\\htdocs\\Bittorrent\\Files\\";
    int retPieceLength = 0;


    public DownloadManager(String name, String tracker, String size, int pieces, String peerId) {
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

    public void readTrackerResponse() {
        ObjectInputStream peerDetailsInputStream = null;
        try {
            peerDetailsInputStream = new ObjectInputStream(downloadManagerSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> peerIds = new ArrayList<String>();
        ArrayList<ArrayList<String>> peerDetails = new ArrayList<ArrayList<String>>();
        try {
            HashMap<String, AbstractList<String>> peers = null;
            try {
                peers = (HashMap<String, AbstractList<String>>) peerDetailsInputStream.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Iterator it = peers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                peerIds.add(String.valueOf(pair.getKey()));
                peerDetails.add((ArrayList<String>) pair.getValue());
                System.out.println(pair.getKey() + " = " + pair.getValue());
//                it.remove(); // avoids a ConcurrentModificationException
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String directory = downloadPath + FilenameUtils.getBaseName(Name);
        for (int i = 0; i < peerIds.size(); i++) {
            String[] tempIp = String.valueOf(peerDetails.get(i).get(0)).split(":");
            if (!(tempIp[1].equalsIgnoreCase("81"))) {
                try {
                    retPieceLength = downloadStrategy(String.valueOf(peerDetails.get(i).get(0)), Integer.parseInt(String.valueOf(peerDetails.get(i).get(2))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("some parts of the file is already present");
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int newNoOfPieces = 0;
        try {
            newNoOfPieces = getNumberParts(directory + "\\" + Name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("no of pieces" + newNoOfPieces);
        System.out.println("return piece length" + retPieceLength);
        if ((Pieces == newNoOfPieces)) {
            //all the pieces are downloaded
            System.out.println("Joining files.....");
            joinFiles();

        }
    }

    public int downloadStrategy(String ip, int pieceLength) throws IOException {
        try {
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
            String[] pieces = new String[pieceLength];
            int[] piecesToDownload = new int[pieceLength];
            while ((inputLine = br.readLine()) != null) {
//                System.out.println("pieces to download\t" + inputLine);
                pieces = inputLine.split("\t");
            }
            br.close();
            for (int i = 0; i < pieces.length; i++) {
                piecesToDownload[i] = Integer.parseInt(pieces[i]);
            }
            sort(piecesToDownload);
            System.out.println("pieces to download from" + newIp);
            for (int i : piecesToDownload) {
                System.out.print(i + "\t");
            }
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -n 1 " + pingIp);
            int returnVal = 1;
            try {
                returnVal = p1.waitFor();
            } catch (InterruptedException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            boolean reachable = (returnVal == 0);
            String checkDownloadPath = downloadPath + FilenameUtils.getBaseName(Name) + "//" + Name;
            if (reachable) {
                try {
                    for (int i = 0; i < pieceLength; i++) {
                        // check if the piece is already downloaded
                        File downloadedPiece = new File(checkDownloadPath + "." + i);
                        boolean exists = downloadedPiece.exists();
                        if (!exists) {
                            retPieceLength++;
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Thread thread = new Thread(new DownloadPiece(BaseURL + "." + piecesToDownload[i], downloadPath + FilenameUtils.getBaseName(Name) + "\\", i));
                            thread.start();
                        } else {
                            System.out.println("Piece" + i + "already downloaded... downloading other pieces");
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
