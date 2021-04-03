package moe.yo3explorer.azustor;

import moe.yo3explorer.azustor.errormodel.ObjectNotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response info()
    {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add("creationTime",bucket.getDateCreated().getTime() / 1000);
        objectBuilder.add("uuid",bucket.getSerialNumber().toString());
        objectBuilder.add("currentTime",System.currentTimeMillis() / 1000);
        objectBuilder.add("version",bucket.getCreatorVersion());
        objectBuilder.add("pid","azustor");

        return Response.ok(objectBuilder.build()).build();
    }

    @POST
    public Response uploadFile(@NotNull byte[] buffer, @NotNull @Context UriInfo uriInfo)
    {
        UUID uuid = bucket.storeFile(buffer);
        logger.infof("Uploaded %d bytes as %s",buffer.length,uuid.toString());

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        uriBuilder.path(uuid.toString());
        return Response.created(uriBuilder.build())
                .header("X-Azustor-UUID",uuid.toString())
                .build();
    }

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@NotNull @PathParam("uuid") UUID uuid)
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
