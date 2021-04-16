package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

/**
 * The super-class for all Azustor exceptions. Catch this if you don't care what happens.
 */
public class AzustorException extends RuntimeException {
    /**
     * Call this to supply a specific error message.
     * @param format The error message
     */
    public AzustorException(String format) {
        super(format);
    }

    /**
     * Call this if an error was caused by an I/O Error.
     * @param e The occurred I/O error.
     */
    public AzustorException(IOException e) {
        super(e);
    }
}
