package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

public class FailedToCreateMasterFileException extends AzustorException {
    public FailedToCreateMasterFileException(IOException e) {
        super(e);
    }
}
