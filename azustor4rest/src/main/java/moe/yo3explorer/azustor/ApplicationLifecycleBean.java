package moe.yo3explorer.azustor;

import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.JsonObject;

/**
 * This class is responsible for creating the bucket when the RESTful service starts up.
 */
@ApplicationScoped
public class ApplicationLifecycleBean {
    @Inject
    Logger logger;

    @Inject
    AzustorResource azustorResource;

    void onStart(@Observes StartupEvent ev)
    {
        logger.info("The application is starting...");
        azustorResource.info().readEntity(JsonObject.class);
    }
}
