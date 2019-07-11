package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.UserDataManager;
import main.types.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserHandler extends HandlerPrototype implements HttpHandler {
    public UserHandler(){
        requiredKeys = new String[] { "token", "action" };
        handlerName = "User Handler";
    }

    @Override
    public void fulfillRequest(JSONObject requestParams) {
        String action = requestParams.getString("action");
        //Route request action to fulfillment method
        switch(action){
            case "create":
                //Create user action defined
                //Determine if request contains required keys for defined keys
                if(isActionKeysValid(new String[]{ "username", "password", "firstname", "lastname", "organization", "access", "token", "action" }, requestParams)) {
                    //Request is valid for defined action, perform request
                    createUser(requestParams);
                } else{
                    //Request is invalid for defined action, return failure notification
                    returnActionFailure();
                }
                break;
            case "edit":
                //Edit user action defined
                if(isActionKeysValid(new String[]{ "fields", "username", "token", "action" }, requestParams)){
                    //Request is valid for edit user action, perform request
                    editUser(requestParams);
                } else {
                    returnActionFailure();
                }
            default:
                //Request defined invalid action type - naturally this is invalid
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

    /**
     * Request is to edit a user object with given parameters
     * @param requestParams edit user request parameters
     */
    private void editUser(JSONObject requestParams){
        UserDataManager userDataManager = new UserDataManager();
        long uid = userDataManager.getUid(requestParams.getString("username"));
        //Parallel arrays to contain edited fields and the new values
        List<String> editFields = new ArrayList<>();
        List<String> editValues = new ArrayList<>();
        //Get each field to be edited and its value
        JSONArray editFieldsArr = requestParams.getJSONArray("fields");
        for(int i = 0; i < editFieldsArr.length(); i++){
            JSONObject thisEditFieldObj = editFieldsArr.getJSONObject(i);
            //Get edit field name
            editFields.add(thisEditFieldObj.getString("fieldname"));
            //Get new value for the field
            editValues.add(thisEditFieldObj.getString("newval"));
        }
        //Perform user edits in user data manager
        if(userDataManager.editUser(uid, editFields, editValues)){
            //Update was successful
            returnActionSuccess();
        } else {
            //Update was unsuccessful
            returnActionFailure();
        }
    }

    /**
     * Get passed user arguments for parameteres object
     * @param requestParams parameters object
     * @return Corresponding user object to parameters
     */
    private User getUserFromParams(JSONObject requestParams){
        //Get values from params json
        String username = requestParams.getString("username");
        String password = requestParams.getString("password");
        String firstName = requestParams.getString("firstname");
        String lastName = requestParams.getString("lastname");
        String org = requestParams.getString("org");
        int access = requestParams.getInt("access");
        //Create user object
        return new User(username, password, firstName, lastName, org, access);
    }

    /**
     * Check request validity using required keys of the defined action
     * @param actionReqKeys required keys defined by the action defined in the original request
     * @param requestParams parameters to verify validity of
     * @return is action valid? boolean
     */
    private boolean isActionKeysValid(String[] actionReqKeys, JSONObject requestParams){
        requiredKeys = actionReqKeys;
        return isRequestValid(requestParams);
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
