package moe.yo3explorer.azustor;

import moe.yo3explorer.azustor.errormodel.ObjectNotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * This class contains the Resources for the RESTful service.
 */
@Singleton
@Path("/")
public class AzustorResource {

    @ConfigProperty(name = "azustor.directory")
    String outputDirectoy;

    @ConfigProperty(name = "azustor.lowMemoryMode")
    boolean lowMemoryMode;

    @ConfigProperty(name = "azustor.volumeSize")
    long volumeSize;

    @Inject
    Logger logger;

    private AzustorBucket bucket;

    /**
     * This method is automatically called by Quarkus once this object gets instanciated. It creates or opens the bucket.
     */
    @PostConstruct
    public void postConstruct()
    {
        logger.info("Hello from the Azustor resource!");

        String properties = String.format(" (%s memory mode,volume size = %d) ",lowMemoryMode ? "low" : "hi",volumeSize);

        File file = new File(outputDirectoy);
        File masterfile = new File(file.getAbsolutePath() + File.separator + "master.cnf");
        logger.info("Looking for " + masterfile.getAbsolutePath());
        if (masterfile.isFile())
        {
            logger.infof("Loading existing store%sfrom: %s ",properties,file.getAbsolutePath());
            bucket = AzustorBucket.loadBucket(file,lowMemoryMode);
        }
        else
        {
            logger.infof("Creating new store%sin: %s ",properties,file.getAbsolutePath());
            bucket = AzustorBucket.createBucket(file,volumeSize,lowMemoryMode);
        }
    }

    /**
     * This method is automatically called by Quarkus when it shuts down.
     */
    @PreDestroy
    public void onShutdown()
    {
        logger.info("Shutting down store");
        try {
            bucket.close();
        } catch (IOException e) {
            logger.warn("Failed to shut down the store cleanly!");
        }
    }

    /**
     * Clients cann call this method to get information about the bucket.
     * @return A JSON-Object with metadata of the Bucket.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Shows information about the bucket.",
                    content = @Content(mediaType = "application/json",
                                       schema = @Schema(implementation = AzustorServerInfo.class)
                    )
            )
    })
    @Operation(summary = "Retrieves information about the bucket.", description = "Displays information about the bucket.")
    public AzustorServerInfo info()
    {
        AzustorServerInfo asi = new AzustorServerInfo();
        asi.pid = "azustor";
        asi.uuid = bucket.getSerialNumber().toString();
        asi.currentTime = System.currentTimeMillis() / 1000;
        asi.creationTime = bucket.getDateCreated().getTime() / 1000;
        asi.creator_version = bucket.getCreatorVersion();

        return asi;
    }

    /**
     * Clients can call this method to put an object into the bucket.
     * @param buffer The object to be uplaoded.
     * @param uriInfo This data gets automatically filled in by Quarkus.
     * @return 201 when the upload is sucessful, 500 when not.
     */
    @POST
    @APIResponses(value = {
            @APIResponse(name = "Sucessful upload",
                    responseCode = "201",
                    headers = {
                        @Header(name = "Location", description = "The URL where the uploaded object may be found."),
                        @Header(name = "X-Azustor-UUID",description = "The UUID of the uploaded object. This UUID is also found in the Location.")
                    }
            ),
            @APIResponse(name = "Upload failed",
            description = "Writing to the bucket failed for whatever reason.",
            responseCode = "500")
    })
    @Operation(summary = "Uploads an object",
            description = "Assigns an UUID to an object and stores it into the bucket. The object must be put into the request body. See AzustorClient and README.md")
    public Response uploadFile(@Parameter(description = "The object to be stored") @NotNull byte[] buffer, @NotNull @Context UriInfo uriInfo)
    {
        UUID uuid = bucket.storeFile(buffer);
        logger.infof("Uploaded %d bytes as %s",buffer.length,uuid.toString());

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(uuid.toString());
        return Response.created(uriBuilder.build())
                .header("X-Azustor-UUID",uuid.toString())
                .build();
    }

    /**
     * Retrieves an object from the bucket.
     * @param uuid The UUID of the object to look for.
     * @return 200 if the object was found, 404 if not.
     */
    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @APIResponses(value = { @APIResponse(
            name = "Object retrieved.",
            description = "The object was sucessfully read from the bucket and may be found in the response body.",
            responseCode = "200",
            content = @Content(mediaType = "application/octet-stream")),
        @APIResponse(
                name = "Object not found",
                description = "An object with the specified UUID was not found in the bucket.",
                responseCode = "404"),
        @APIResponse(
                name = "I/O error",
                description = "An I/O error occurred when scanning or reading the bucket.",
                responseCode = "500")
    })
    public Response getFile(@Parameter(name = "uuid",
            description = "The UUID of the object you want to retrieve. Can be found in the Location or X-Azustor-UUID headers when storing the object.",
            required = true,
            allowEmptyValue = false,
            example = "244d99b6-d3c5-4935-9f56-77a5e8614e07"
    ) @NotNull @PathParam("uuid") UUID uuid)
    {
        logger.info("Looking for " + uuid.toString());
        try {
            byte[] bytes = bucket.retrieveFile(uuid);
            return Response.ok(bytes).build();
        }
        catch (ObjectNotFoundException onfe) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
