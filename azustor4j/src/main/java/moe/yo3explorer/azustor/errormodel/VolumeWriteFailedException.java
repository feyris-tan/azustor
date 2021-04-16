package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

/**
 * This exception is thrown when a write operation to a volume file fails.
 */
public class VolumeWriteFailedException extends AzustorException{
    /**
     * Generates this exception.
     * @param e The I/O error that occurred.
     */
    public VolumeWriteFailedException(IOException e) {
        super(e);
    }
}
