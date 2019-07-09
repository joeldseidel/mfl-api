package main.handlers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.sun.net.httpserver.HttpHandler;
import main.managers.UserDataManager;
import org.json.JSONObject;

/**
 * @author Joel Seidel
 * Handler to determine authenticity of a user credentials ~ login process
 */
public class AuthenticateUserHandler extends HandlerPrototype implements HttpHandler {
    public AuthenticateUserHandler(){
        requiredKeys = new String[] { "username", "password" };
        handlerName = "Authenticate User Handler";
    }

    /**
     * Authenticate user by username and password
     * @param requestParams username and password params json object
     */
    @Override
    public void fulfillRequest(JSONObject requestParams) {
        //Get values from request parameter object
        String username = requestParams.getString("username");
        String password = requestParams.getString("password");
        //Get user credential authenticity
        boolean isUserValid = new UserDataManager().isUserValid(username, password);
        //Create authenticity return object
        JSONObject userResponseObj = new JSONObject();
        userResponseObj.put("user_valid", isUserValid);
        if(isUserValid){
            //Add token for future requests
            userResponseObj.put("token", createToken());
        }
        //Return authenticity object to client
        this.response = userResponseObj.toString();
    }

    /**
     * Override the base class request validity check. The client has not yet been assigned a token
     * @param requestParams parameters from the client to be validated
     * @return validity of the request
     */
    @Override
    protected boolean isRequestValid(JSONObject requestParams) {
        if(requestParams == null){
            //There are no request params
            return false;
        }
        for(String requiredKey : requiredKeys){
            if(!requestParams.has(requiredKey)){
                //Missing a required key, request is invalid
                return false;
            }
        }
        return true;
    }

    /**
     * Create token for connected client
     * @return token string
     */
    private String createToken(){
        String token = "";
        try{
            //Generate a user token
            Algorithm algorithm = Algorithm.HMAC256("secret");
            token = JWT.create().withIssuer("localhost:1680").sign(algorithm);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return token;
    }
}
