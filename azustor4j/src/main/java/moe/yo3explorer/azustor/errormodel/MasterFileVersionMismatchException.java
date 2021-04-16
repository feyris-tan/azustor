package moe.yo3explorer.azustor.errormodel;

/**
 * This exception gets thrown when you try to open a bucket, but it requires a newer version of this library.
 */
public class MasterFileVersionMismatchException extends MasterFileUnreadableException {
    /**
     * Generates this exception.
     * @param creatorVersion The version you actually need.
     * @param runningVersion The version you have.
     */
    public MasterFileVersionMismatchException(int creatorVersion, int runningVersion) {
        super(String.format("This bucket is intended for at least Azustor Version %d, however you are running %d",creatorVersion,runningVersion));
    }
}
