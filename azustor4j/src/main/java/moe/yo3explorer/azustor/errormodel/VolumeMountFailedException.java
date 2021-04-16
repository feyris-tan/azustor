package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

/**
 * This exception gets thrown when Azustor couldn't accesss a specific volume file.
 */
public class VolumeMountFailedException extends AzustorException{
    /**
     * Generates this exception.
     * @param e The I/O exception that occurred when opening a volume file.
     */
    public VolumeMountFailedException(IOException e) {
        super(e);
    }
}
