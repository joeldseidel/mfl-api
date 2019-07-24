package main.types;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

/**
 * Represents a patient record - mirrors patient record in EHR via FHIR
 */
public class PatientProfile {
    private String gender, city, state, race, ethnicity, birthSex, ehrKey;
    private int age;
    private boolean isActive, isDeceased;

    /**
     * Default constructor
     */
    public PatientProfile(){}

    /**
     * Constructor to create patient profile from EHR patient record
     * @param patientRecord patient record object from EHR
     */
    public PatientProfile(JSONObject patientRecord) {
        //Convert patient record to object properties
        parseProfileFromRecord(patientRecord);
    }

    /**
     * Get patient profile properties from a patient record object from EHR
     * @param patientRecord patient record object from EHR
     */
    private void parseProfileFromRecord(JSONObject patientRecord){
        //Get property values from EHR object
        this.gender = patientRecord.getJSONObject("gender").getString("value");
        this.city = getCity(patientRecord);
        this.state = getState(patientRecord);
        this.age = getYearsOld(patientRecord.getJSONObject("birthDate").getString("value"));
        this.isActive = patientRecord.getJSONObject("active").getBoolean("value");
        this.isDeceased = patientRecord.getJSONObject("deceasedBoolean").getBoolean("value");
        this.ehrKey = patientRecord.getJSONObject("id").getString("value");
        //Get variable extension values from extension object in patient record object
        parseRecordExtensions(patientRecord);
    }

    /**
     * Convert this object to a representative JSON object
     * @return JSON object representing this object
     */
    public JSONObject convertToJson(){
        JSONObject thisProfile = new JSONObject();
        //Add object properties to JSON object
        thisProfile.put("gender", this.gender);
        thisProfile.put("city", this.city);
        thisProfile.put("state", this.state);
        thisProfile.put("age", this.age);
        thisProfile.put("isActive", this.isActive);
        thisProfile.put("isDeceased", this.isDeceased);
        return thisProfile;
    }

    /**
     * Get the age of patient based on date of birth
     * @param birthDate date of birth string from patient record
     * @return integer value of years old
     */
    private int getYearsOld(String birthDate) {
        int yearsOld;
        try {
            //Convert string -> date -> local date
            LocalDate dob = new SimpleDateFormat("yyyy-MM-dd").parse(birthDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate now = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            //Get difference between now and dob in years
            yearsOld = Period.between(dob, now).getYears();
        } catch (ParseException pEx) {
            //This will not happen ~ thanks FHIR
            yearsOld = 0;
        }
        return yearsOld;
    }

    /**
     * Get city name from address object array
     * @param patientRecord patient data record
     * @return city name from address object
     */
    private String getCity(JSONObject patientRecord) {
        //Get address object marked as home
        JSONObject thisAddress = getHomeAddressObj(patientRecord);
        return thisAddress.getJSONObject("city").getString("value");
    }

    /**
     * Get state name from address object array
     * @param patientRecord patient data record
     * @return state name from address object
     */
    private String getState(JSONObject patientRecord) {
        //Get address object marked as home
        JSONObject thisAddress = getHomeAddressObj(patientRecord);
        return thisAddress.getJSONObject("state").getString("value");
    }

    /**
     * Get address object marked as home from patient record object
     * @param patientRecord patient record object
     * @return JSON object of home address object
     */
    private JSONObject getHomeAddressObj(JSONObject patientRecord){
        //Get all addresses in array
        try{
            JSONArray addressRecords = patientRecord.getJSONArray("address");
            for(int i = 0; i < addressRecords.length(); i++){
                JSONObject thisAddress = addressRecords.getJSONObject(i);
                //Get address type marker
                String useValue = thisAddress.getJSONObject("use").getString("value");
                if(useValue.equals("home")){
                    //This is the home address
                    return thisAddress;
                }
            }
            //There must be a home address ~ code unreachable but here to quell the compilers concerns
            return new JSONObject();
        } catch(JSONException jEx) {
            //There is only one address - there is no array because it is an object
            return patientRecord.getJSONObject("address");
        }
    }

    /**
     * Parse the extensions to the medical record for race/ethnicity/birth sex
     * @param patientRecord patient record object from EHR
     */
    private void parseRecordExtensions(JSONObject patientRecord){
        //Get array of the extensions contained within the patient record
        JSONArray recordExtensions = patientRecord.getJSONArray("extension");
        for(int i = 0; i < recordExtensions.length(); i++) {
            JSONObject thisExtension = recordExtensions.getJSONObject(i);
            //Get type of the extension to determine property representation
            switch(thisExtension.getString("url")){
                case "http://hl7.org/fhir/StructureDefinition/us-core-race":
                    //Get the race extension value
                    this.race = getExtensionValue(thisExtension);
                    break;
                case "http://hl7.org/fhir/StructureDefinition/us-core-ethnicity":
                    //Get the ethnicity extension value
                    this.ethnicity = getExtensionValue(thisExtension);
                    break;
                case "http://hl7.org/fhir/StructureDefinition/us-core-birth-sex":
                    //Get the birth sex extension value
                    this.birthSex = getExtensionValue(thisExtension);
                    break;
            }
        }
    }

    /**
     * Get text value of an extension object on an EHR record object
     * @param extension extension object
     * @return string of text value of the extension object property
     */
    private String getExtensionValue(JSONObject extension) {
        //Get extension value wrapper
        JSONObject valueCodeConcept = extension.getJSONObject("valueCodeableConcept");
        //Get text value of extension
        return valueCodeConcept.getJSONObject("text").getString("value");
    }

    /**
     * Gender property getter method
     * @return string: gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * City property getter method
     * @return string: city
     */
    public String getCity() {
        return city;
    }

    /**
     * State property getter method
     * @return string: state
     */
    public String getState() {
        return state;
    }

    /**
     * Race property getter method
     * @return string: race
     */
    public String getRace() {
        return race;
    }

    /**
     * Ethnicity property getter method
     * @return string: ethnicity
     */
    public String getEthnicity() {
        return ethnicity;
    }

    /**
     * Birth sex property getter method
     * @return string: birth sex
     */
    public String getBirthSex() {
        return birthSex;
    }

    /**
     * EHR key property getter method
     * @return string: EHR key
     */
    public String getEhrKey() {
        return ehrKey;
    }

    /**
     * Age property getter method
     * @return integer: age
     */
    public int getAge() {
        return age;
    }

    /**
     * Is active profile getter method
     * @return boolean: is active?
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Is deceased profile getter method
     * @return boolean: is deceased?
     */
    public boolean isDeceased() {
        return isDeceased;
    }
}
