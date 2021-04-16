package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

/**
 * This exception gets thrown when the control file can't be read for any reason
 */
public class MasterFileUnreadableException extends AzustorException {
    /**
     * An I/O exception occurred when reading the control file.
     * @param e The I/O exception that occurred.
     */
    public MasterFileUnreadableException(IOException e) {
        super(e);
    }

    /**
     * Some arbitrary error occurred when reading the control file.
     * @param s A human readable error message.
     */
    public MasterFileUnreadableException(String s) {
        super(s);
    }
}
