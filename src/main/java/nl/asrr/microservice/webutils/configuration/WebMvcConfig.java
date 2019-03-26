package nl.asrr.microservice.webutils.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.asrr.microservice.webutils.StringToEnumConverterFactory;
import nl.asrr.microservice.webutils.amqp.FailableListenerAspect;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.format.FormatterRegistry;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

public abstract class WebMvcConfig extends WebMvcConfigurationSupport {

    @Bean
    public FailableListenerAspect failableListenerAspect() {
        return new FailableListenerAspect();
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();

        // add additional information during serialization to enable deserialization
        // of polymorphic object models
        objectMapper.enableDefaultTyping();

        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToEnumConverterFactory());
    }

}
