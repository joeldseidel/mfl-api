package test;

import main.managers.EHRDataManager;
import main.types.PatientProfile;
import main.types.exceptions.EHRQueryException;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

/**
 * EHR Data Manager test
 *
 * NOTE: all tests are run from Epic's sandbox at open.epic.com
 */
public class __EHRDataManager {
    @Test
    public void __EHRQueryByName(){
        EHRDataManager ehrDataManager = new EHRDataManager();
        try{
            PatientProfile[] patientProfiles = ehrDataManager.getPatientDataByName("Argonaut", "Jason");
            assertEquals(patientProfiles.length, 2);
            PatientProfile patientProfile = patientProfiles[0];
            //Check against the expected sandbox values
            assertEquals(patientProfile.getAge(), 33);
            assertEquals(patientProfile.getBirthSex(), "Male");
            assertEquals(patientProfile.getCity(), "Verona");
            assertEquals(patientProfile.getState(), "WI");
            assertEquals(patientProfile.getGender(), "male");
            assertEquals(patientProfile.getEthnicity(), "Not Hispanic or Latino");
            assertEquals(patientProfile.getEhrKey(), "Tbt3KuCY0B5PSrJvCu2j-PlK.aiHsu2xUjUM8bWpetXoB");
        } catch (EHRQueryException ehrEx){
            fail(ehrEx.getErrorMessage());
        }
    }
}
