package moe.yo3explorer.azustor.errormodel;

public class MasterFileInvalidMagicException extends MasterFileUnreadableException {
    public MasterFileInvalidMagicException()
    {
        super("This master file is in an unexpected format!");
    }
}
