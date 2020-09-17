package NaFtp.DataConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class FtpDataConnectionActive  extends Thread implements ImplFtpDataConnectionServer{

    private InetAddress address;
    private InetAddress loaclAddress;
    private int port;
    private boolean isWaiting=false;
    private Socket ftpDataClientSocket;


    public FtpDataConnectionActive(InetAddress localAddress, InetAddress address, int port) {
        this.loaclAddress=address;
    this.address= address;
    this.port= port;
    isWaiting=true;
    }

    @Override
    public void run() {
        super.run();
        try {
            ftpDataClientSocket = new Socket(address, port, loaclAddress, 20);
            isWaiting=false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isWaiting() {
        return isWaiting;
    }

    @Override
    public Socket getFtpDataClientSocket() {
        return ftpDataClientSocket;
    }
}
