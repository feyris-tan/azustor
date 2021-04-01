package moe.yo3explorer.azustor.errormodel;

public class MasterFileVersionMismatchException extends MasterFileUnreadableException {
    public MasterFileVersionMismatchException(int creatorVersion, int runningVersion) {
        super(String.format("This bucket is intended for at least Azustor Version %d, however you are running %d",creatorVersion,runningVersion));
    }
}
