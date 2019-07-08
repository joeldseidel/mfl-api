package main.managers;

import main.data.DatabaseInteraction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Joel Seidel
 * Manager for user data ~ contains helper functions for data interaction and processing
 */
public class UserDataManager {
    private DatabaseInteraction database;
    public UserDataManager(){
        this.database = new DatabaseInteraction();
    }

    /**
     * Determine if user is valid by username and password
     * @param username username to validate
     * @param password password to validate
     */
    public boolean isUserValid(String username, String password){
        //Get count of the rows that match the username and password provided
        String getCredsValidSql = "SELECT COUNT(*) FROM Users WHERE username = ? AND password = ?";
        PreparedStatement getCredsValidStmt = database.prepareStatement(getCredsValidSql);
        //Get hashed password to compare to database
        String hashedPassword = getHashedPassword(password);
        boolean isValid = false;
        try {
            //Prepare statement arguments
            getCredsValidStmt.setString(1, username);
            getCredsValidStmt.setString(2, hashedPassword);
            //Run query for match count
            ResultSet userCredsValidResult = database.query(getCredsValidStmt);
            //Credentials are valid if count of matching rows is exactly one
            isValid = userCredsValidResult.getInt(0) == 1;
        } catch (SQLException sqlEx){
            sqlEx.printStackTrace();
            //Not sure of actual validity, but the query didn't work
            isValid = false;
        }
        return isValid;
    }

    /**
     * Get hashed string for password
     * @param password plain text password to hash
     * @return hashed text password
     */
    private String getHashedPassword(String password){
        MessageDigest messageDigestSHA;
        try {
            //Get SHA-256 algorithm
            messageDigestSHA = MessageDigest.getInstance("SHA-256");
            //Hash password string
            return new String(messageDigestSHA.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException nsae){
            nsae.printStackTrace();
        }
        return null;
    }
}