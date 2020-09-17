package NaFtp.CommandConnection;

import Database.DatabaseHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class FtpServer {
    public static  String fileDirectory = "D:\\NaFtpDir\\";
    public static  String driveLabel = "D:";
    public static final int PORT = 21;
    public static final DatabaseHandler DBH = new DatabaseHandler();

    public FtpServer() {
        ServerSocket ftpServerSocket = null;
        try {
            ftpServerSocket = new ServerSocket(PORT);
            System.out.println("server started");
            while (!ftpServerSocket.isClosed()) {
                new FtpService(ftpServerSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ftpServerSocket != null)
                    ftpServerSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        if(args.length>0) {
            fileDirectory = args[0];
            driveLabel = fileDirectory.substring(0, 2);
        }
        System.out.println("Starting Server From: "+fileDirectory);
        new FtpServer();
    }
    public static InetAddress getIpAddress(){
        try {
            InetAddress myIP=InetAddress.getLocalHost();
            return myIP;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
