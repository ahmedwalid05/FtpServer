package Database;

import models.FtpUser;

import java.sql.*;

public class DatabaseHandler implements DataBaseKeyWords {
    private Connection conn;

    public DatabaseHandler() {
//        try {
//            conn = DriverManager.getConnection("jdbc:sqlserver://;Server=192.168.100.12;port=1433;Network Library=DBMSSOCN;Database=naftpdb;user=naif;password=12345");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    public boolean checkIfUserExists(String username) {
        return username.equals("guest");
//        try {
//            PreparedStatement preparedStatement = conn.prepareStatement("select username from users where username = ?");
//            preparedStatement.setString(1, username);
//            return preparedStatement.executeQuery().next();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
    }

    public boolean checkIfPasswordIsCorrectAndCreateUser(FtpUser user, String pass) {
        return user.getUsername().equals("guest") && pass.equals("guest");
//        try {
//            PreparedStatement preparedStatement = conn.prepareStatement("select * from users where username = ? and password = ?");
//            preparedStatement.setString(1, user.getUsername());
//            preparedStatement.setString(2, pass);
//            ResultSet set = preparedStatement.executeQuery();
//            if (set.next()) {
//                user.setUserID(set.getInt(USER_ID_COLUMN));
//                user.setCanDelete(set.getBoolean(CAN_DELETE_COLUMN));
//                user.setCanDownload(set.getBoolean(CAN_DOWNLOAD_COLUMN));
//                user.setCanUpload(set.getBoolean(CAN_UPLOAD_COLUMN));
//                System.out.println("login successful");
//                return true;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
    }
}
