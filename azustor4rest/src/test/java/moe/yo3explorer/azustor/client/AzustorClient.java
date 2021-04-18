package moe.yo3explorer.azustor.client;

import moe.yo3explorer.azustor.AzustorServerInfo;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * This is a client to communicate with an AzustorResource using Microprofile's REST Client
 */
@Singleton
@Path("/")
@RegisterRestClient(configKey = "azustor-client")
public interface AzustorClient {
    /**
     * Retrieves Metadata for the bucket.
     * @return Meta information of the bucket.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    AzustorServerInfo getServerInfo();

    /**
     * Stores an object into the bucket.
     * @param buffer The object to be stored.
     * @return 201 if the object was uploaded sucessfully, 500 if not. Check the "Location" or "X-Azustor-UUID" header to retrieve it later.
     */
    @POST
    Response uploadFile(@NotNull byte[] buffer);

    /**
     * Fetches an object from the bucket.
     * @param uuid The UUID of the object to look for.
     * @return 200 if the object was found. Use readEntity(byte[].class) to read it, 404 if the object doesn't exist.
     */
    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response getFile(@NotNull @PathParam("uuid") UUID uuid);
}
