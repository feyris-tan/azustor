package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

/**
 * This exception gets thrown when a read operation on a volume file failed.
 */
public class VolumeReadException extends AzustorException {
    /**
     * Generates this exception.
     * @param ioe The I/O exception that occurred when the read operation failed.
     */
    public VolumeReadException(IOException ioe) {
        super(ioe);
    }
}
