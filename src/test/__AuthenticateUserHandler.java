package test;

import main.handlers.AuthenticateUserHandler;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

public class __AuthenticateUserHandler {
    @Test
    public void __ValidCredentials(){
        JSONObject validCredsObj = getCredsObj("ralphnader", "forpresident");
        AuthenticateUserHandler handler = new AuthenticateUserHandler();
        handler.fulfillRequest(validCredsObj);
        String handlerResponseStr = handler.getResponse();
        assertNotNull(handlerResponseStr);
        JSONObject resultObj = new JSONObject(handlerResponseStr);
        assertTrue(resultObj.getBoolean("user_valid"));
        assertTrue(resultObj.has("token"));
    }

    @Test
    public void __InvalidCredentials(){
        JSONObject invalidCredsObj = getCredsObj("algore", "inventedtheinternet");
        AuthenticateUserHandler handler = new AuthenticateUserHandler();
        handler.fulfillRequest(invalidCredsObj);
        String handlerResponseStr = handler.getResponse();
        assertNotNull(handlerResponseStr);
        JSONObject resultObj = new JSONObject(handlerResponseStr);
        assertFalse(resultObj.getBoolean("user_valid"));
        assertFalse(resultObj.has("token"));
    }

    private JSONObject getCredsObj(String username, String password){
        JSONObject credsObj = new JSONObject();
        credsObj.put("username", username);
        credsObj.put("password", password);
        return credsObj;
    }
}