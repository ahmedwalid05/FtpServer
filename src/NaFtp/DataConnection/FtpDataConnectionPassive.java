package NaFtp.DataConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FtpDataConnectionPassive extends Thread implements ImplFtpDataConnectionServer {
    private int port;
    private Socket ftpDataClientSocket;
    private ServerSocket dataServerSocket;
    private boolean isWaiting = false;
    public FtpDataConnectionPassive(int port) {
        this.port = port;

        try {
            isWaiting = true;
            System.out.println("Trying to bind: "+ port);
             dataServerSocket = new ServerSocket(port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try {

            System.out.println("isWaiting for connection");
            ftpDataClientSocket = dataServerSocket.accept();
            isWaiting =false;
            System.out.println("data socket accepted: " + ftpDataClientSocket.getInetAddress().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getFtpDataClientSocket() {
        return ftpDataClientSocket;
    }


    public boolean isWaiting() {
        return isWaiting;
    }


}
