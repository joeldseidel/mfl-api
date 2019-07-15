package main.managers;

import main.data.DatabaseInteraction;
import main.types.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;

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
        boolean isValid;
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
     * Get user id of a user record by username
     * @param username username to retrieve uid of
     * @return uid of the provided username
     */
    public long getUid(String username){
        //Get uid from user record by username
        String getUidSql = "SELECT uid FROM Users WHERE username = ?";
        PreparedStatement getUidStmt = database.prepareStatement(getUidSql);
        try {
            //Prepare get uid args
            getUidStmt.setString(1, username);
            ResultSet getUidResults = database.query(getUidStmt);
            if(getUidResults.next()){
                //Return the uid of username
                return getUidResults.getLong("uid");
            } else {
                //Nothing in result set
                return -1;
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            return -1;
        }
    }

    /**
     * Create user record in database from user object
     * @param userProfile user object containing record params
     * @return success / failure boolean
     */
    public boolean createUser(User userProfile){
        boolean isSuccessfulInsert;
        //Insert user record to database
        String insertUserSql = "INSERT INTO Users (username, password, firstname, lastname, organizationid, accessid) VALUES(?, ?, ?, ?, ?, ?)";
        PreparedStatement insertUserStmt = database.prepareStatement(insertUserSql);
        try {
            //Prepare statement with user parameters
            insertUserStmt.setString(1, userProfile.getUsername());
            insertUserStmt.setString(2, getHashedPassword(userProfile.getPassword()));
            insertUserStmt.setString(3, userProfile.getFirstname());
            insertUserStmt.setString(4, userProfile.getLastname());
            insertUserStmt.setInt(5, userProfile.getOrganization());
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
     * Edit user record in database by field, defined by user
     * @param uid user id
     * @param editFields user attributes to edit
     * @param newValues new values for user attribute
     * @return success / failure boolean
     */
    public boolean editUser(long uid, List<String> editFields, List<String> newValues){
        boolean isSuccessfulUpdate;
        //Create base clause of the update string
        StringBuilder updateUserSql = new StringBuilder("UPDATE Users SET ");
        //Add field set clauses for each of the fields to be edited
        for(int i = 0; i < editFields.size(); i++){
            //Add field name ~ corresponding to the database field value
            updateUserSql.append(editFields.get(i));
            updateUserSql.append(" = ");
            updateUserSql.append("'");
            //Add new field value
            updateUserSql.append(newValues.get(i));
            updateUserSql.append("'");
            if(i + 1 < editFields.size()){
                //This is not the last field to be edited, add the comma and space
                updateUserSql.append(", ");
            }
        }
        //Done adding fields, add the where clause
        updateUserSql.append(" WHERE uid = ? ");
        try {
            //Prepare and run update nonquery on built query string
            PreparedStatement updateUserStmt = database.prepareStatement(updateUserSql.toString());
            updateUserStmt.setLong(1, uid);
            database.nonQuery(updateUserStmt);
            isSuccessfulUpdate = true;
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            isSuccessfulUpdate = false;
        }
        return isSuccessfulUpdate;
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

            IntStream.range(0, hash.length).mapToObj(i -> Integer.toHexString(0xff & hash[i])).forEach(hex -> {
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            });

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}