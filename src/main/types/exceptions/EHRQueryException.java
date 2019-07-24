package main.types.exceptions;

/**
 * Exception raised when an error occurs within an EHR query
 * This exception exists mostly for semantic purposes
 */
public class EHRQueryException extends Exception {
    public EHRQueryException(String queryError, Throwable rootError){
        super(queryError, rootError);
    }
    public EHRQueryException(String queryError){
        super(queryError);
    }
}
