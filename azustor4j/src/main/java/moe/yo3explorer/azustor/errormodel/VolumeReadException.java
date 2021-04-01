package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

public class VolumeReadException extends AzustorException {
    public VolumeReadException(IOException ioe) {
        super(ioe);
    }
}
