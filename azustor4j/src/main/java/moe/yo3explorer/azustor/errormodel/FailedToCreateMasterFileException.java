package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

/**
 * This exception is thrown when an I/O error occurred while creating the control file.
 */
public class FailedToCreateMasterFileException extends AzustorException {
    /**
     * Generates this exception.
     * @param e The I/O exception that occurred when creating the control file.
     */
    public FailedToCreateMasterFileException(IOException e) {
        super(e);
    }
}
