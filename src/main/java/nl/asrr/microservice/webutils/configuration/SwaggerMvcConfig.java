package nl.asrr.microservice.webutils.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public abstract class SwaggerMvcConfig extends WebMvcConfig {

    private static final Class[] ignoredModels = {
            InputStream.class, File.class, URI.class, URL.class
    };

    @Value("${spring.application.name}")
    private String applicationName;

    public abstract Docket api();

    public Docket build(String baseControllerPackage) {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(baseControllerPackage))
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(metaData())
                .ignoredParameterTypes(ignoredModels);
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder()
                .title(String.format("ASRR %s RESTful API", applicationName))
                .description(String.format("ASRR RESTful API for %s", applicationName))
                .contact(new Contact(
                        "ASRR Software Development",
                        "https://asrr.nl",
                        "support@asrr.nl"
                ))
                .build();
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}