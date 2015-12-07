/**
 * Created by Rakesh on 11/13/2015.
 */


import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;

/**
 * get the .torrent file extract the url of the tracker and request for details of other peers
 */
public class Peer_Client {
    private static Socket socket;
    private static int PORT = 8090; //tracker port
    private static String trackerURL = "localhost";
    private static String torrentsFolder = "C:\\Users\\Rakesh\\IdeaProjects\\Torrents\\";
    private static String peerID = "Peer1";
    private static String splitFiles = "D:\\xampp\\htdocs\\Bittorrent\\Files\\";

    public static void main(String[] args) throws IOException {
        //create a menu to request user input to either create a torrent or open existing one
//        while (true) {
        System.out.println("Select an option from the menu below:");
        System.out.println("1-- Create Torrent");
        System.out.println("2-- Open Existing Torrent");
        Scanner userChoice = new Scanner(System.in);
        PrintWriter writer_log;

        //Creating the logfile
        writer_log = new PrintWriter(new FileWriter("clientlog.txt", true));

        DateFormat date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        Calendar calender = Calendar.getInstance();
        writer_log.println(date.format(calender.getTime()));
        writer_log.println("----------Advanced SE-----------");
        writer_log.println("********CLIENT LOG*******");
        writer_log.println("-----------PROJECT 1-----------------");

        // For Creating new torrent
        if (userChoice.nextInt() == 1) {
            TorrentFile torrentFile = new TorrentFile();
            // Open a file chooser and get the file
            JFileChooser fileChooser = new JFileChooser();
            JFrame f = new JFrame();
            f.setTitle("New Torrent");
            writer_log.println(date.format(calender.getTime()));
            writer_log.println("New torrent");
            f.setAlwaysOnTop(true);
            int returnVal = fileChooser.showOpenDialog(f);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("Creating new torrent...");
                String baseName;
                File inputFile = fileChooser.getSelectedFile();
                torrentFile.setTrackerUrl(trackerURL);
                baseName = FilenameUtils.getBaseName(inputFile.getName());
                File outputFile = new File(splitFiles + baseName);
                if (!outputFile.exists()) {
                    outputFile.mkdir();
                }
                int piece = new FileSplitter().split(inputFile.getParent() + "\\" + inputFile.getName(), splitFiles + baseName + "\\");
                torrentFile.setPiece(piece);
                torrentFile.setFileName(inputFile.getName());
                //create a torrent file using the input file
                String path = torrentsFolder + baseName + ".torrent";
                File tFile = new File(path);
                PrintWriter writer = new PrintWriter(tFile);
                writer.println("Name-" + torrentFile.getFileName());
                writer.println("Tracker-" + trackerURL);
                writer.println("File Size-" + inputFile.length());
                writer.println("Pieces-" + torrentFile.getPiece());
                writer.close();

                //open socket and send peer details to tracker
                try {
                    socket = new Socket("localhost", PORT);
                    System.out.println("Connected to Tracker...");
                    writer_log.println(date.format(calender.getTime()));
                    writer_log.println("Connected to tracker");

                } catch (Exception e) {
                    System.out.println("Could not connect to Tracker try again , Error --" + e.toString());
                    writer_log.println(date.format(calender.getTime()));
                    writer_log.println("Could not connect to Tracker try again , Error --" + e.toString());
                }
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                String tempIP = InetAddress.getLocalHost() + ":81";
                System.out.println("registering with tracker...");
                printWriter.write("register" + "," + torrentFile.getFileName() + "," + peerID + "," + tempIP + "," + 81 + "," + piece);
                printWriter.flush();
                socket.close();
                printWriter.close();

            }
        } else {
            String Name = null, Size = null, Pieces = null, Tracker = null;
            JFileChooser fileChooser = new JFileChooser(new File(torrentsFolder));
            JFrame f = new JFrame();
            f.setTitle("Open Existing Torrent");
            writer_log.println(date.format(calender.getTime()));
            writer_log.println("Opening Existing torrent");
            f.setAlwaysOnTop(true);
            int returnVal = fileChooser.showOpenDialog(f);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Bit Torrent File", "torrent");
            fileChooser.setFileFilter(filter);
            writer_log.println(date.format(calender.getTime()));
            writer_log.println("Torrent Selected");
            File inputFile = fileChooser.getSelectedFile();
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("Opening existing torrent...");
                if (inputFile.exists()) {
                    FileReader torrentFile = null;
                    try {
                        torrentFile = new FileReader(inputFile.getParent() + "\\" + inputFile.getName());
                    } catch (FileNotFoundException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    try {
                        BufferedReader reader = new BufferedReader(torrentFile);
                        String line = null;
                        String[] temp;
                        while ((line = reader.readLine()) != null) {
                            temp = line.split("-");
                            if (temp[0].equals("Name")) {
                                Name = temp[1];
                                System.out.println("\nName: " + Name);
                            } else if (temp[0].equals("Tracker")) {
                                Tracker = temp[1];
                                System.out.println("\nTracker: " + Tracker);
                            } else if (temp[0].equals("File Size")) {
                                Size = temp[1];
                                System.out.println("\nFile Size: " + Size);
                            } else if (temp[0].equals("Pieces")) {
                                Pieces = temp[1];
                                System.out.println("\nNumber of Pieces: " + Pieces);
                            }
                        }
                        reader.close();
                    } catch (IOException x) {
                        System.err.println(x);
                    }

                    Thread client = new DownloadManager(Name, Tracker, Size, Integer.parseInt(Pieces), peerID);
                    client.start();
                }
            }

        }
        writer_log.close();
//        }
    }
}

