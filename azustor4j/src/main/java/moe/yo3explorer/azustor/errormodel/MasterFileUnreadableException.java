package moe.yo3explorer.azustor.errormodel;

import java.io.IOException;

public class MasterFileUnreadableException extends AzustorException {
    public MasterFileUnreadableException(IOException e) {
        super(e);
    }

    public MasterFileUnreadableException(String s) {
        super(s);
    }
}
