package no.sanderolin.boligbot.housingimport.exception;

/**
 * Custom exception for housing import operations.
 * Used to wrap and categorize different types of errors that can occur during housing data import.
 */
public class HousingImportException extends RuntimeException {

    public HousingImportException(String message) {
        super(message);
    }

    public HousingImportException(String message, Throwable cause) {
        super(message, cause);
    }
}