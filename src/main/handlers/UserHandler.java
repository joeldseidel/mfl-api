package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.UserDataManager;
import main.types.User;
import org.json.JSONObject;

public class UserHandler extends HandlerPrototype implements HttpHandler {
    public UserHandler(){
        requiredKeys = new String[] { "token", "action" };
        handlerName = "User Handler";
    }

    @Override
    public void fulfillRequest(JSONObject requestParams) {
        String action = requestParams.getString("action");
        switch(action){
            case "create":
                requiredKeys = new String[] { "username", "password", "firstname", "lastname", "organization", "access", "token", "action" };
                if(isRequestValid(requestParams)) {
                    createUser(requestParams);
                } else{
                    returnActionFailure();
                }
                break;
            default:
                returnActionFailure();
        }
    }

    /**
     * Request is to create a user object with given parameters
     * @param requestParams create user request parameters
     */
    private void createUser(JSONObject requestParams){
        User createdUser = getUserFromParams(requestParams);
        UserDataManager userDataManager = new UserDataManager();
        if(userDataManager.createUser(createdUser)){
            returnActionSuccess();
        } else {
            returnActionFailure();
        }
    }

    private User getUserFromParams(JSONObject requestParams){
        String username = requestParams.getString("username");
        String password = requestParams.getString("password");
        String firstName = requestParams.getString("firstname");
        String lastName = requestParams.getString("lastname");
        String org = requestParams.getString("org");
        int access = requestParams.getInt("access");
        return new User(username, password, firstName, lastName, org, access);
    }

    /**
     * The action was invalid in some way
     */
    private void returnActionFailure(){
        this.response = new JSONObject().put("success", false).toString();
    }

    /**
     * The action was successful, report back to the client
     */
    private void returnActionSuccess(){
        this.response = new JSONObject().put("success", true).toString();
    }
}
