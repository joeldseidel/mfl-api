package main.handlers;

import com.sun.net.httpserver.HttpHandler;
import main.managers.EHRDataManager;
import main.types.PatientProfile;
import main.types.exceptions.EHRQueryException;
import org.json.JSONObject;

public class PatientHandler extends HandlerPrototype implements HttpHandler {
    public PatientHandler(){
        requiredKeys = new String[] { "action", "token" };
        handlerName = "Patient Handler";
    }

    private enum Action { CREATE, IMPORT }

    @Override
    public void fulfillRequest(JSONObject requestParams) {
        Action action = Action.valueOf(requestParams.getString("action").toUpperCase());
        switch(action){
            case CREATE:
                //Action is to create new user profile
                //TODO: figure out the create procedure
                break;
            case IMPORT:
                //Action is to import a patient profile
                if(isActionKeysValid(new String[] {"family", "given", "token", "action"}, requestParams)){
                    EHRDataManager ehrDataManager = new EHRDataManager();
                    String familyName = requestParams.getString("family");
                    String givenName = requestParams.getString("given");
                    try {
                        //Get patient profiles matching the names provided
                        PatientProfile[] patientProfiles = ehrDataManager.getPatientDataByName(familyName, givenName);
                        //Create the return arg containing the patient profiles in JSON format
                        JSONObject returnArgs = new JSONObject().put("profiles", ehrDataManager.convertProfilesToJsonArray(patientProfiles));
                        //Action was success, include the matching results as return args
                        returnActionSuccess(returnArgs);
                    } catch(EHRQueryException ehrEx) {
                        //Action was not successful
                        returnActionFailure();
                    }
                }
                break;
            default:

        }
    }
}
