package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

public class AzustorException extends RuntimeException {
    public AzustorException(String format) {
        super(format);
    }

    public AzustorException(IOException e) {
        super(e);
    }
}
