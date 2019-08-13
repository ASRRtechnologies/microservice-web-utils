package nl.asrr.microservice.webutils.configuration.swagger;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
@ExcludeControllerDoc
public class SwaggerController {

    @GetMapping(value = "/docs", produces = MediaType.TEXT_HTML_VALUE)
    public String docs() throws IOException {
        InputStream resource = SwaggerController.class.getResourceAsStream("/META-INF/resources/swagger-ui.html");
        byte[] targetArray = new byte[resource.available()];
        resource.read(targetArray);
        return new String(targetArray);
    }

}
