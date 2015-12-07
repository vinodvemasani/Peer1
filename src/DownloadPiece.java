/**
 * Created by Rakesh on 11/23/2015.
 */

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.*;

public class DownloadPiece implements Runnable {
    String fileURL = "";
    String destinationDirectory = "";
    int piece;
    Socket updateSocket;

    DownloadPiece(String fURL, String dDirectory, int piece) {
        destinationDirectory = dDirectory;
        fileURL = fURL;
        this.piece = piece;
    }

    public void run() {
        download(fileURL, destinationDirectory, piece);
    }

    public synchronized void download(String fileURL, String destinationDirectory, int piece) {
        // File name that is being downloaded
        String downloadedFileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);
        File downloadsDir = new File(destinationDirectory);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        // Open connection to the file
        URL url = null;
        try {
            url = new URL(fileURL);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        InputStream is = null;
        try {
            is = url.openStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Stream to the destination file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destinationDirectory + downloadedFileName);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Read bytes from URL to the local file
        byte[] buffer = new byte[1048576];
        int bytesRead = 0;

        try {
            while ((bytesRead = is.read(buffer)) != -1) {
                System.out.print(".");  // Progress bar :)
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Downloaded piece " + downloadedFileName );
        // Close destination stream
        try {
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Close URL stream
        try {
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            //update the info file

//            System.out.println("inside finally finished downloading" + piece);
            try {
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(destinationDirectory + "//" + "info.ase", true)));
                //get the piece from file url
//                System.out.println("piece " + piece);
                writer.print(piece + "\t");
                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                updateSocket = new Socket("localhost", 8090);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Updating XMl in the tracker to indicate this peer also has the piece"+piece);
            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(updateSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String tempIP = null;
            try {
                tempIP = InetAddress.getLocalHost() + ":81";
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
//            downloadedFileName.substring(0,downloadedFileName.length()-2)

            System.out.println("updating" + "," + FilenameUtils.getBaseName(fileURL) + "," + "Peer1" + "," + tempIP + "," + 81 + "," + 1);
            printWriter.println("updating" + "," + FilenameUtils.getBaseName(fileURL) + "," + "Peer1" + "," + tempIP + "," + 81 + "," + 1);
            printWriter.flush();
        }
    }
}
