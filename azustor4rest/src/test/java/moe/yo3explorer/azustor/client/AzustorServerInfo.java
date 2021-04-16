package moe.yo3explorer.azustor.client;

/**
 * This is an entity class which contains metadata about an Azustor bucket.
 */
public class AzustorServerInfo {
    /**
     * The Unix timestamp of when the bucket was first created.
     */
    public long creationTime;
    /**
     * The unique identifier of the bucket. This does not correspond to an object in the bucket.
     */
    public String uuid;
    /**
     * The Unix timestamp of the current clock state of the server.
     */
    public long currentTime;
    /**
     * The Azustor version that created the container.
     */
    public int version;
    /**
     * Always "azustor"
     */
    public String pid;
}
