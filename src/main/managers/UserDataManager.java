package main.managers;

import main.data.DatabaseInteraction;
import main.types.User;

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
        String getCredsValidSql = "SELECT password FROM Users WHERE username = ?";
        PreparedStatement getCredsValidStmt = database.prepareStatement(getCredsValidSql);
        //Get hashed password to compare to database
        String hashedPassword = getHashedPassword(password);
        boolean isValid = false;
        try {
            //Prepare statement arguments
            getCredsValidStmt.setString(1, username);
            //Run query for match count
            ResultSet userCredsValidResult = database.query(getCredsValidStmt);
            isValid = userCredsValidResult.next() && userCredsValidResult.getString("password").equals(hashedPassword);
        } catch (SQLException sqlEx){
            sqlEx.printStackTrace();
            //Not sure of actual validity, but the query didn't work
            isValid = false;
        }
        return isValid;
    }

    /**
     * Create user record in database from user object
     * @param userProfile user object containing record params
     * @return success / failure boolean
     */
    public boolean createUser(User userProfile){
        boolean isSuccessfulInsert;
        //Insert user record to database
        String insertUserSql = "INSERT INTO Users (username, password, firstname, lastname, organizationid, access) VALUES(?, ?, ?, ?, ?, ?)";
        PreparedStatement insertUserStmt = database.prepareStatement(insertUserSql);
        try {
            //Prepare statement with user parameters
            insertUserStmt.setString(1, userProfile.getUsername());
            insertUserStmt.setString(2, userProfile.getPassword());
            insertUserStmt.setString(3, userProfile.getFirstname());
            insertUserStmt.setString(4, userProfile.getLastname());
            insertUserStmt.setString(5, userProfile.getOrganization());
            insertUserStmt.setInt(6, userProfile.getAccess());
            //Insert user record
            database.nonQuery(insertUserStmt);
            isSuccessfulInsert = true;
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            isSuccessfulInsert = false;
        }
        return isSuccessfulInsert;
    }

    /**
     * Get hashed string for password
     * @param password plain text password to hash
     * @return hashed text password
     */
    private String getHashedPassword(String password){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}