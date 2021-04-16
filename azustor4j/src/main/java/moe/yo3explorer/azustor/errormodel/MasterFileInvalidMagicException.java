package moe.yo3explorer.azustor.errormodel;

/**
 * This exception gets thrown when the control file is an invalid control file.
 */
public class MasterFileInvalidMagicException extends MasterFileUnreadableException {
    /**
     * Generates this exception
     */
    public MasterFileInvalidMagicException()
    {
        super("This master file is in an unexpected format!");
    }
}
