package main.managers;

import main.types.PatientProfile;
import main.types.exceptions.EHRQueryException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Manager of data interaction between MFL and EHR system
 */
public class EHRDataManager {

    private static final String ehrUrl = "https://open-ic.epic.com/FHIR/api/FHIR/DSTU2/Patient?";

    /**
     * Perform EHR api search using given and family name parameters
     * @param family last name
     * @param given first name
     * @return patient profile object from query results
     */
    public PatientProfile[] getPatientDataByName(String family, String given) throws EHRQueryException {
        //Add patient profile search with name parameters
        String fhirQuery = ehrUrl + "family=" + family + "&given=" + given;
        //Perform EHR api query
        String ehrResultString = doEhrQuery(fhirQuery);
        JSONObject ehrObj;
        try {
            ehrObj = XML.toJSONObject(ehrResultString);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EHRQueryException("Invalid EHR Query result string", ex);
        }
        int resultCount = getSearchResultCount(ehrObj);
        PatientProfile[] patientDataProfiles;
        if(resultCount == 0){
            //Empty result set for provided name
            throw new EHRQueryException("Patient could not be located in EHR");
        } else {
            //Patient exists ~ though more than one record may exist for it
            patientDataProfiles = getSearchResultsArray(ehrObj);
        }
        return patientDataProfiles;
    }

    /**
     * Perform query on EHR and return response string in FHIR format
     * @return query results string
     */
    private String doEhrQuery(String queryStr){
        StringBuilder ehrResponse = new StringBuilder();
        try {
            //Create and perform EHR API connection
            URL queryUrl = new URL(queryStr);
            BufferedReader reader = new BufferedReader(new InputStreamReader(queryUrl.openStream(), "UTF-8"));
            for(String line; (line = reader.readLine()) != null;){
                //Read FHIR response string line by line and add to total results string
                ehrResponse.append(line);
            }
        } catch (IOException ioEx) {
            //A problem :(
            ioEx.printStackTrace();
        }
        return ehrResponse.toString();
    }

    /**
     * Get the search results as patient profiles
     * @param ehrObj EHR query result JSON object
     * @return array of patient profile objects representing the patients in the search results
     */
    private PatientProfile[] getSearchResultsArray(JSONObject ehrObj){
        //Get the root element of the patients results
        JSONArray resultArr = ehrObj.getJSONObject("Bundle").getJSONArray("entry");
        //Initialize an array to the size of the search results
        PatientProfile[] patientProfiles = new PatientProfile[getSearchResultCount(ehrObj)];
        for(int i = 0; i < resultArr.length(); i++){
            //Get the patient object from results array
            JSONObject patientSearchResultObj = resultArr.getJSONObject(i);
            JSONObject patientObj = patientSearchResultObj.getJSONObject("resource").getJSONObject("Patient");
            //Create patient profile object
            PatientProfile thisPatient = new PatientProfile(patientObj);
            //Add patient to corresponding profile array
            patientProfiles[i] = thisPatient;
        }
        return patientProfiles;
    }

    private int getSearchResultCount(JSONObject ehrObj){
        if(!ehrObj.has("Bundle")){
            //Missing bundle root element ~ there cannot be results
            return 0;
        }
        JSONObject bundleRootObj = ehrObj.getJSONObject("Bundle");
        //Get the number of results in the search result set
        return bundleRootObj.getJSONObject("total").getInt("value");
    }
}