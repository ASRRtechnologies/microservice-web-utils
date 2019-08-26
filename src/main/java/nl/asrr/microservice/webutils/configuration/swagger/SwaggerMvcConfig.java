package nl.asrr.microservice.webutils.configuration.swagger;

import com.google.common.base.Predicate;
import nl.asrr.microservice.webutils.configuration.WebMvcConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;

@EnableSwagger2
public abstract class SwaggerMvcConfig extends WebMvcConfig {

    private static final Class[] ignoredModels = {
            InputStream.class, File.class, URI.class, URL.class, AuthenticationPrincipal.class
    };

    private static final String BASE_PACKAGE = "nl.asrr.microservice";

    @Value("${spring.application.name}")
    private String applicationName;


    @Bean
    public Docket defaultDocket() {
        return build(BASE_PACKAGE);
    }

    public Docket build(String baseControllerPackage) {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(excludeAnnotation(ExcludeControllerDoc.class))
                .apis(RequestHandlerSelectors.basePackage(baseControllerPackage))
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(metaData())
                .ignoredParameterTypes(ignoredModels);
    }

    private static Predicate<RequestHandler> excludeAnnotation(final Class<? extends Annotation> annotation) {
        return handler -> handler == null || !handler.findControllerAnnotation(annotation).isPresent();
    }

    @Bean
    public SwaggerController swaggerController() {
        return new SwaggerController();
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
        super.addResourceHandlers(registry);

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}
