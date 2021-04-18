package moe.yo3explorer.azustor;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * This is an entity class which contains metadata about an Azustor bucket.
 */
@Schema(description = "Contains information about the bucket.")
public class AzustorServerInfo {
    /**
     * The Unix timestamp of when the bucket was first created.
     */
    @Schema(description = "The Unix timestamp of when the bucket was first created.")
    public long creationTime;
    /**
     * The unique identifier of the bucket. This does not correspond to an object in the bucket.
     */
    @Schema(description = "The unique identifier of the bucket. This does not correspond to an object in the bucket.")
    public String uuid;
    /**
     * The Unix timestamp of the current clock state of the server.
     */
    @Schema(description = "The Unix timestamp of the current clock state of the server.")
    public long currentTime;
    /**
     * The Azustor version that created the container.
     */
    @Schema(description = "The Azustor version that created the container.")
    public int creator_version;
    /**
     * Always "azustor"
     */
    @Schema(description = "Always contains the string \"azustor\"")
    public String pid;
}
