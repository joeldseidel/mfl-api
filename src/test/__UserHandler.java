package test;

import main.data.DatabaseInteraction;
import main.handlers.UserHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class __UserHandler {
    private static final String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJsb2NhbGhvc3Q6Njk2OSJ9.BtqugmuS7W-RcSHk_iiTwSk10DNgcmnUhKoe9JFTAqk";
    @Test
    public void __CreateUser(){
        createTestUserProfile();
        DatabaseInteraction database = new DatabaseInteraction();
        ResultSet createUserResults = database.query(database.prepareStatement("SELECT * FROM Users WHERE username = 'realDonaldTrump'"));
        try {
            assertTrue(createUserResults.next());
            assertEquals("realDonaldTrump", createUserResults.getString("username"));
            assertEquals("1f4f83bcd491b78b4b4b533676d6fe1cd0d6892f0cfb59548b5e461274d32ec9", createUserResults.getString("password"));
            assertEquals("Donald", createUserResults.getString("firstname"));
            assertEquals("Trump", createUserResults.getString("lastname"));
            assertEquals(69, createUserResults.getInt("organizationid"));
            assertEquals(1, createUserResults.getInt("accessid"));
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            fail();
        }
        //Test has passed. Clean up database entries
        database.nonQuery(database.prepareStatement("DELETE FROM Users WHERE username = 'realDonaldTrump'"));
        database.closeConnection();
    }

    @Test
    public void __EditUser(){
        createTestUserProfile();
        UserHandler handler = new UserHandler();
        JSONObject editRequest = new JSONObject();
        editRequest.put("action", "edit");
        editRequest.put("token", token);
        JSONArray editFields = new JSONArray();
        editRequest.put("username", "realDonaldTrump");
        JSONObject editFirstNameObj = new JSONObject();
        editFirstNameObj.put("fieldname", "firstname");
        editFirstNameObj.put("newval", "Vladimir");
        editFields.put(editFirstNameObj);
        JSONObject editLastNameObj = new JSONObject();
        editLastNameObj.put("fieldname", "lastname");
        editLastNameObj.put("newval", "Putin");
        editFields.put(editLastNameObj);
        editRequest.put("fields", editFields);
        handler.fulfillRequest(editRequest);
        DatabaseInteraction database = new DatabaseInteraction();
        ResultSet editUserResults = database.query(database.prepareStatement("SELECT * FROM Users WHERE username = 'realDonaldTrump'"));
        try{
            editUserResults.next();
            assertEquals("Vladimir", editUserResults.getString("firstname"));
            assertEquals("Putin", editUserResults.getString("lastname"));
        } catch(SQLException sqlEx) {
            fail();
        }
        //Clean up database entries
        database.nonQuery(database.prepareStatement("DELETE FROM Users WHERE username = 'realDonaldTrump'"));
        database.closeConnection();
    }

    private void createTestUserProfile(){
        JSONObject createUserRequest = new JSONObject();
        createUserRequest.put("action", "create");
        createUserRequest.put("token", token);
        createUserRequest.put("username", "realDonaldTrump");
        createUserRequest.put("password", "thanksobama");
        createUserRequest.put("firstname", "Donald");
        createUserRequest.put("lastname", "Trump");
        createUserRequest.put("organization", 69);
        createUserRequest.put("access", 1);
        UserHandler handler = new UserHandler();
        handler.fulfillRequest(createUserRequest);
    }
}
