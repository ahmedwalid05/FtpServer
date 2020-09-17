package models;

public class FtpUser {
    private int userID;
    private boolean isLoggedIn=false;
    private String username;
    private boolean canDownload;
    private boolean canUpload;
    private boolean canDelete;

    public FtpUser(int userID, String username, boolean canDownload, boolean canUpload, boolean canDelete) {
        this.userID = userID;
        this.username = username;
        this.canDownload = canDownload;
        this.canUpload = canUpload;
        this.canDelete = canDelete;
    }

    public FtpUser(String username) {
        this.username = username;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isCanDownload() {
        return canDownload;
    }

    public void setCanDownload(boolean canDownload) {
        this.canDownload = canDownload;
    }

    public boolean isCanUpload() {
        return canUpload;
    }

    public void setCanUpload(boolean canUpload) {
        this.canUpload = canUpload;
    }


    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
