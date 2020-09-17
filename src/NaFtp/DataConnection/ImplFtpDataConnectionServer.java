package NaFtp.DataConnection;

import java.net.Socket;

public interface ImplFtpDataConnectionServer  {


    boolean isWaiting();
    Socket getFtpDataClientSocket();
    void start();


}
