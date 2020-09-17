package NaFtp.DataConnection;

import NaFtp.Codes.DataOperationCodes;
import NaFtp.CommandConnection.FtpServer;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class DataService extends Thread implements DataOperationCodes {

    private File ftpFile;
    private int dataOperation;
    private Socket dataServiceSocket;
    private DataOutputStream bufferOut;
    private DataInputStream bufferIn;
    private String fileToDownload;

//    public DataService(int dataOperation, Socket dataServiceSocket, String ftpDir) {
//        this.dataOperation = dataOperation;
//        this.dataServiceSocket = dataServiceSocket;
//        this.ftpDir = ftpDir;
//    }
    public DataService(int dataOperation, Socket dataServiceSocket, File ftpDir) {
        this.dataOperation = dataOperation;
        this.dataServiceSocket = dataServiceSocket;
        this.ftpFile= ftpDir;
    }

    @Override
    public void run() {
        super.run();
        try {

            bufferIn = new DataInputStream(dataServiceSocket.getInputStream());
            bufferOut = new DataOutputStream(dataServiceSocket.getOutputStream());
            switch (dataOperation) {
                case LIST:
                    System.out.println("starting list operation");
                    listFilesAndRespond();
                    break;
                case DOWNLOAD_FILE_FROM_SERVER:
                    System.out.println("starting download from server operation");
                    convertTheFileToBytesAndSendToClient(fileToDownload);
                    break;
                case STORE_FILE_IN_SERVER:
                    System.out.println("Storing incoming file");
                    receiveBytesFromClientAndStore(fileToDownload);
                    break;
            }
            bufferIn.close();
            bufferOut.close();

        } catch (IOException e) {
            e.printStackTrace(); // todo handle exception, maybe throw interrupt exception
        }

    }


    private void listFilesAndRespond() throws IOException {
        try {
            File[] files = ftpFile.listFiles();
            if (files != null) {
                for (File value : files) {
                    FileOwnerAttributeView  attributes = Files.getFileAttributeView(value.toPath(), FileOwnerAttributeView.class);
                    String d = value.isDirectory() ? "d" : "-";
                    String x = value.canExecute() ? "x" : "-";
                    String r = value.canRead() ? "r" : "-";
                    String w = value.canWrite() ? "w" : "-";
                    String permission = d + x + r + w;
                    permission = permission+permission+permission;
                    String[] owners = attributes.getOwner().getName().split("\\\\");
                    String length = value.isDirectory()? String.valueOf((Objects.requireNonNull(value.listFiles()).length)) : String.valueOf(value.length());
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm");
                    String listResponse = permission+"    "+ length+ " "+ owners[0]+ "     "+ owners[1]+"           "+ value.length()+" "+ sdf.format(value.lastModified()) +" " + value.getName().trim()+"\r\n";
                    bufferOut.write(listResponse.getBytes());
                    bufferOut.flush();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void convertTheFileToBytesAndSendToClient(String fileToDownload) {

        System.out.println(ftpFile.getAbsolutePath());
        FileInputStream fileInputStream = null;
        try {
            byte[] dataBuffer = new byte[1024]; //byte array of data that will be sent (IN KB)
            fileInputStream = new FileInputStream(ftpFile); //reading from the file
            //reading the bytes of the file
            while (fileInputStream.read(dataBuffer) != -1) {
                bufferOut.write(dataBuffer);
            }
            bufferOut.write("\r\n".getBytes());
            bufferOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } //closing the file stream
        }
    }

    private void receiveBytesFromClientAndStore(String fileToDownload) throws IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(ftpFile);
        byte[] data = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int byteRead;

        try{
            while(true){
                byteRead =bufferIn.readByte();
//            bos.write(byteRead);
                fileOutputStream.write(byteRead);
            }
        }catch (EOFException e){
            System.out.println("Data Received");
        }

//        bos.flush();
        fileOutputStream.close();
//        do {
//
//            bos.write(bufferIn.readByte());
//             bytesRead = bufferIn.read(data);
//            fileOutputStream.write(data);
//        }while (bytesRead!=-1);
//        fileOutputStream.close();
        System.out.println("file storage successful done");
    }


    public void setArgument(String fileToDownload) {
        this.fileToDownload = fileToDownload;
    }
}
