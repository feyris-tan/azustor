package moe.yo3explorer.azustor.client;

import io.quarkus.test.junit.QuarkusTest;
import moe.yo3explorer.azustor.AzustorServerInfo;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Random;
import java.util.UUID;

/**
 * This is a QuarkusTest that shows how to communicate with the AzustorResource
 */
@QuarkusTest
public class AzustorClientTest {

    @Inject
    @RestClient
    AzustorClient myClient;

    @Inject
    Logger logger;

    /**
     * This method gets executed in order to run the test.
     */
    @Test
    public void testIt()
    {
        AzustorServerInfo serverInfo = myClient.getServerInfo();
        Assertions.assertNotEquals(0,serverInfo.creationTime);
        Assertions.assertNotEquals(0,serverInfo.currentTime);
        Assertions.assertEquals("azustor",serverInfo.pid);
        Assertions.assertNotNull(serverInfo.uuid);
        Assertions.assertNotEquals(0,serverInfo.creator_version);

        Random random = new Random();

        for (int i = 0; i < 100; i++) {
            int expectedLength = random.nextInt(Short.MAX_VALUE);
            byte[] expectedData = new byte[expectedLength];
            random.nextBytes(expectedData);

            //Ein File hochladen
            Response uploadResponse = myClient.uploadFile(expectedData);

            //UUID vom Upload holen
            String uploadedUuidStri = uploadResponse.getHeaders().getFirst("X-Azustor-UUID").toString();
            UUID uploadedUuid = UUID.fromString(uploadedUuidStri);
            logger.info("Uploaded as %s" + uploadedUuid.toString());

            //Ein File herunterladen
            Response file = myClient.getFile(uploadedUuid);
            byte[] actualData = file.readEntity(byte[].class);

            Assertions.assertEquals(expectedLength, actualData.length);
            Assertions.assertArrayEquals(expectedData, actualData);
        }
    }
}
