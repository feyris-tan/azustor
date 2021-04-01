package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

public class VolumeWriteFailedException extends AzustorException{
    public VolumeWriteFailedException(IOException e) {
        super(e);
    }
}
