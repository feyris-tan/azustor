package moe.yo3explorer.azustor;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

import javax.ws.rs.core.Application;

/**
 * This class provides the OpenAPI Definition
 */
@OpenAPIDefinition(info = @Info(title = "Azustor RESTful Interface",
version = "1.0",
contact = @Contact(
        name = "Feyris-Tan",
        url = "https://github.com/feyris-tan/azustor",
        email = "azustor@feyris-tan.worse-than.tv"),
        license = @License(name = "BSD-3-Clause", url = "https://opensource.org/licenses/BSD-3-Clause")
))
public class AzustorApiApplication extends Application {
}
