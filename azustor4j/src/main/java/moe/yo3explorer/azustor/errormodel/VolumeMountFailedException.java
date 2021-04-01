package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

public class VolumeMountFailedException extends AzustorException{
    public VolumeMountFailedException(IOException e) {
        super(e);
    }
}
