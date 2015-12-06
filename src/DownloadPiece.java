/**
 * Created by Rakesh on 11/23/2015.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadPiece implements Runnable {
    String fileURL = "";
    String destinationDirectory = "";

    DownloadPiece(String fURL, String dDirectory) {
        destinationDirectory = dDirectory;
        fileURL = fURL;
    }

    public void run() {
        download(fileURL, destinationDirectory);
    }

    public synchronized void download(String fileURL, String destinationDirectory) {
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

        System.out.println("Downloading " + downloadedFileName);
        try {
            while ((bytesRead = is.read(buffer)) != -1) {
                System.out.print(".");  // Progress bar :)
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("done!");

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
        }
    }
}
