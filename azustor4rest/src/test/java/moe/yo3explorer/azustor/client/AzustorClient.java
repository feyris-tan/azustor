package moe.yo3explorer.azustor.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Singleton
@Path("/")
@RegisterRestClient(configKey = "azustor-client")
public interface AzustorClient {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    AzustorServerInfo getServerInfo();

    @POST
    Response uploadFile(@NotNull byte[] buffer);

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response getFile(@NotNull @PathParam("uuid") UUID uuid);
}
