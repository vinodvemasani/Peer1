/**
 * Created by Rakesh on 11/14/2015.
 */
public class TorrentFile {
//    String filePath;
    String trackerUrl;

    public int getPiece() {
        return piece;
    }

    public void setPiece(int piece) {
        this.piece = piece;
    }

    int piece;
    String fileName;
//    String Comments;

//    public String getFilePath() {
//        return filePath;
//    }

//    public void setFilePath(String filePath) {
//        this.filePath = filePath;
//    }

    public String getTrackerUrl() {
        return trackerUrl;
    }

    public void setTrackerUrl(String trackerUrl) {
        this.trackerUrl = trackerUrl;
    }



    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

//    public String getComments() {
//        return Comments;
//    }

//    public void setComments(String comments) {
//        Comments = comments;
//    }


}
