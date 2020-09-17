package NaFtp.CommandConnection;

import NaFtp.Codes.ClientCodes;
import NaFtp.Codes.DataOperationCodes;
import NaFtp.Codes.ServerResponses;
import NaFtp.DataConnection.DataService;
import NaFtp.DataConnection.FtpDataConnectionActive;
import NaFtp.DataConnection.FtpDataConnectionPassive;
import NaFtp.DataConnection.ImplFtpDataConnectionServer;
import models.FtpUser;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Random;


public class FtpService extends Thread implements ClientCodes, ServerResponses {
    private Socket clientSocket;
    private String ftpClientDir = FtpServer.fileDirectory;
    public static final int STOP_BIT = -1;
    public static final int PORTS_CONSTANT = 256;

    private DataInputStream bufferIn; // to transmit the bytes to the client
    private DataOutputStream bufferOut; // to receive the bytes from the client
    private FtpUser ftpUser;
    private ImplFtpDataConnectionServer dataServer;

    public FtpService(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            System.out.println("client connected");
            bufferIn = new DataInputStream(clientSocket.getInputStream());
            bufferOut = new DataOutputStream(clientSocket.getOutputStream());
            writeToStream(WELCOME_MSG); // sending welcome message

            this.start();
        } catch (IOException e) {
            try {
                if (bufferIn != null)
                    bufferIn.close();
                if (bufferOut != null)
                    bufferOut.close();
                if (this.clientSocket != null)
                    this.clientSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            String command;
            while (!clientSocket.isClosed()) {
                byte[] payload = new byte[1024];
                int bytesRead = bufferIn.read(payload);
                if (bytesRead != STOP_BIT) {
                    command = new String(payload);
                    handleCommand(command.trim());
                }
            }
        } catch (IOException e) {
            try {
                if (bufferOut != null) {
                    bufferOut.close();
                }
                if (clientSocket != null)
                    clientSocket.close();
                if (bufferIn != null)
                    bufferIn.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //todo if the dataserver thread or dataservice thread where interrupted the buffer should send in complete task
        }
    }


    private void handleCommand(String command) throws IOException, InterruptedException {
        System.out.println("Command: " + command);
        String[] args = command.split(" ");

        if (ftpUser == null || !ftpUser.isLoggedIn()) {
            switch (args[0].toUpperCase()) {
                case AUTH:
                    handleAuth(args);
                    break;
                case USER:
                    handleUser(args);
                    break;
                case PASS:
                    handlePass(args);
                    break;
                default:
                    writeToStream(NOT_LOGGED_IN);
                    break;
            }
        } else
            switch (args[0].toUpperCase()) {
                case SYST:
                    handleSyst();
                    break;
                case PWD:
                case XPWD:
                    handlePwd();
                    break;
                case PASV:
                    handlePassive();
                    break;
                case PORT:
                    handlePort(args);
                    break;
                case TYPE:
                    handleType();
                    break;
                case NLST:
                case LIST:
                    handleList();
                    break;
                case RETR:
                    handleDownload(args);
                    break;
                case STOR:
                    handleUpload(args);
                    break;
                case XMKD:
                case MKD:
                    handleMakeDir(args);
                    break;
                case XRMD:
                case RMD:
                case DELE:
                    handleDelete(args);
                    break;
                case CWD:
                    handleChangeDir(args);
                    break;
                case CDUP:
                    handleChangeToParentDir();
                    break;
                case SIZE:
                    handleSize(args);
                    break;
                case QUIT:
                    handleDisconnect();
                    break;
                default:
                    writeToStream(COMMAND_NOT_IMPLEMENTED_RESPONSE);


            }

    }

    private void handlePort(String[] args) {
        ClientSource source = getClientAddressFromCommand(args[1]);
        dataServer = new FtpDataConnectionActive(clientSocket.getInetAddress(), source.getIpAddress(), source.getPort());
        dataServer.start();
        writeToStream(ACTIVE_REQUEST_ACCEPTED_RESPONSE);
    }

    private void handlePwd() throws IOException {
        String path = PATH_NAME_CREATED_RESPONSE + " \"" + ftpClientDir + "\"\r\n";
        writeToStream(path);

    }

    private void handleSyst() throws IOException {
        writeToStream(SYSTEM_NAME_RESPONSE);

    }

    private void handlePassive() throws IOException {

        Random random = new Random();
        int min = 49152;
        int max = 65535;
        int port = random.nextInt(max - min) + min;

        int p1 = port / PORTS_CONSTANT;
        int p2 = port % PORTS_CONSTANT;


        dataServer = new FtpDataConnectionPassive(port);
        dataServer.start();
        InetAddress address = FtpServer.getIpAddress();
        String ip  = address.getHostAddress().replace(".", ",");
        String passiveDataPortMsg = ENTERING_PASSIVE_MODE_RESPONSE + "("+ip+"," + p1 + "," + p2 + ")\r\n";

        writeToStream(passiveDataPortMsg);

        System.out.println("passive mode activated");

    }

    private void handleType() throws IOException {
        writeToStream(REQUEST_ACCEPTED_RESPONSE);

    }

    private void handleList() throws IOException, InterruptedException {

        while (dataServer.isWaiting()) ;
        if (dataServer == null || dataServer.getFtpDataClientSocket() == null || dataServer.getFtpDataClientSocket().isClosed()) {
            writeToStream(NO_DATA_CONNECTION);
        } else {
            writeToStream(OPENING_BINARY_MODE);
            File file = new File(ftpClientDir);
            DataService service = new DataService(DataOperationCodes.LIST, dataServer.getFtpDataClientSocket(), file);
            service.start();
            service.join();
            writeToStream(DATA_CONNECTION_COMMAND_COMPLETE);

            System.out.println("List complete");
        }
    }

    private void handleDownload(String[] args) throws IOException, InterruptedException {
//        if (ftpUser.isCanDownload()) {


        while (dataServer.isWaiting()) ;
        String fileToDownload = getCorrectArgument(args);
        writeToStream(OPENING_BINARY_MODE);
        File file = new File(ftpClientDir + fileToDownload);
        if (file.exists()) {

            DataService service = new DataService(DataOperationCodes.DOWNLOAD_FILE_FROM_SERVER, dataServer.getFtpDataClientSocket(), file);
//        service.setArgument(fileToDownload.trim());
            service.start();
            service.join();
            writeToStream(DATA_CONNECTION_COMMAND_COMPLETE);
            System.out.println("file downloaded from server");
        } else {
            writeToStream(FILE_DIRECTORY_NOT_FOUND);
        }


    }

    private void handleUpload(String[] args) throws IOException, InterruptedException {

        String fileToStore = getCorrectArgument(args);
        while (dataServer.isWaiting()) ;
        writeToStream(OPENING_BINARY_MODE);
        File file = new File(ftpClientDir + fileToStore.trim());
        DataService service = new DataService(DataOperationCodes.STORE_FILE_IN_SERVER, dataServer.getFtpDataClientSocket(), file);
//        service.setArgument(fileToStore.trim());
        service.start();
        service.join();
        writeToStream(DATA_CONNECTION_COMMAND_COMPLETE);

        System.out.println("file stored to server"); //todo convert this string to a constant and other similar prints and use them in logger

    }

    private void handleMakeDir(String[] args) {
        String directoryToMake = getCorrectArgument(args);

        if (createDirectory(directoryToMake.trim()))
            writeToStream(DIRECTORY_CREATE_OPERATION_SUCCESS.replace("${pathname}", directoryToMake));
        else {
            writeToStream(DIRECTORY_CREATE_OPERATION_FAILED);
        }

    }

    private void handleDelete(String[] args) {
        String fileOrDirectoryToDelete = getCorrectArgument(args);
        if (deleteFileOrDirectory(fileOrDirectoryToDelete.trim()))
            writeToStream(File_DIRECTORY_DELETE_OPERATION_SUCCESS.replace("${}", fileOrDirectoryToDelete));
        else
            writeToStream(File_DIRECTORY_DELETE_OPERATION_FAILED.replace("${}", fileOrDirectoryToDelete));


    }

    private void handleChangeDir(String[] args) {
        String path = getCorrectArgument(args);
        if (setTheCorrectPath(path))
            writeToStream(DIRECTORY_LOCATION_CHANGED.replace("${}", path));

        else
            writeToStream(FILE_DIRECTORY_NOT_FOUND);

    }

    private void writeToStream(String message) {
        try {
            System.out.println("Writing: [" + message.replace("\r\n", "") + "]");
            bufferOut.write(message.getBytes());
            bufferOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleChangeToParentDir() {
        ftpClientDir = FtpServer.fileDirectory;
        writeToStream(DATA_CONNECTION_COMMAND_COMPLETE);

    }

    private void handleDisconnect() {
        try {
            writeToStream(POSITIVE_LOGOUT_RESPONSE);
            if (dataServer != null && dataServer.getFtpDataClientSocket() != null && !dataServer.getFtpDataClientSocket().isClosed())
                dataServer.getFtpDataClientSocket().close();
            clientSocket.close();
            System.out.println("Ended Connection");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleSize(String[] args) {
        String path = getCorrectArgument(args);
//        setTheCorrectPath(path);
        long size = getDirectorySize(path);
        if (size != -1) {
            String positiveResponse = SIZE_POSITIVE_RESPONSE + size + "\r\n";
            writeToStream(positiveResponse);
        } else {
            writeToStream(SIZE_NEGATIVE_RESPONSE);
        }

    }

    private void handleAuth(String[] args) throws IOException {
        if (args[1].equalsIgnoreCase(TLS)) {
            writeToStream(SIZE_NEGATIVE_RESPONSE);
        }
    }

    private void handleUser(String[] args) throws IOException {
        if (FtpServer.DBH.checkIfUserExists(args[1])) {
            ftpUser = new FtpUser(-1, args[1], true, true, true);
            writeToStream(NEED_PASSWORD_RESPONSE);
        } else {
            writeToStream(INVALID_USERNAME_PASSWORD_RESPONSE);
//            handleDisconnect();
        }

    }

    private void handlePass(String[] args) throws IOException {
        if (FtpServer.DBH.checkIfPasswordIsCorrectAndCreateUser(ftpUser, args[1])) {
            ftpUser.setLoggedIn(true);
            writeToStream(POSITIVE_USER_RESPONSE);
        } else {
            writeToStream(INVALID_USERNAME_PASSWORD_RESPONSE);
//            handleDisconnect();
        }

    }

    private long getDirectorySize(String fileLocation) {
        File file = new File(fileLocation);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                return Objects.requireNonNull(files).length;
            } else
                return file.length();
        } else
            return -1;
    }

    private boolean setTheCorrectPath(String path) {
        String location;
        if (path.equals("..") || path.contains(".."))
            location = FtpServer.fileDirectory;
        else if (path.contains(FtpServer.driveLabel))
            location = path + "\\";
        else
            location = ftpClientDir + path + "\\";
        if (getDirectorySize(location) == -1)
            return false;
        ftpClientDir = location;
        return true;


    }

    public String getCorrectArgument(String[] args) {
        String correctArgument = args[1];
        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                correctArgument += " " + args[i];
            }
        }
        return correctArgument;
    }

    public ClientSource getClientAddressFromCommand(String command) {
        String[] args = command.split(",");
        String address = args[0] + "." + args[1] + "." + args[2] + "." + args[3];
        int port = Integer.parseInt(args[4]) * PORTS_CONSTANT + Integer.parseInt(args[5]);
        try {

            ClientSource clientSource = new ClientSource(port, Inet4Address.getByName(address));
            return clientSource;
        } catch (UnknownHostException e) {
            e.printStackTrace();

        }
        return null;
    }


    private boolean deleteFileOrDirectory(String fileOrDirectoryToDelete) {
        File file = new File(ftpClientDir + fileOrDirectoryToDelete);
        boolean deleted = file.delete();
        System.out.println("Directory delete: " + deleted);
        return deleted;
    }

    private boolean createDirectory(String directoryToMake) {
        File dirc = new File(ftpClientDir + directoryToMake);
        boolean created = dirc.mkdir();

        System.out.println("Directory Made: " + created);
        return created;
    }

    final class ClientSource {
        private int port;
        private InetAddress ipAddress;

        public ClientSource(int port, InetAddress ipAddress) {
            this.port = port;
            this.ipAddress = ipAddress;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public InetAddress getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(InetAddress ipAddress) {
            this.ipAddress = ipAddress;
        }
    }
// todo replace all print statements with logger
    //todo should i close the serversocket or is it handled by jgc??
    //todo regular string expression used to print or log as constants
    //todo handle active ftp issues
    //todo rename variable names to something suitable
    //todo log messages should make sense
    //todo fix the issue that causes CWD /D:\NaFtpDir\..\ in winscp
    //todo make sure ur ftp server works with google chrome
}
