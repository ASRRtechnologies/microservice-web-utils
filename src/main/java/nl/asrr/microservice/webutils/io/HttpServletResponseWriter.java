package nl.asrr.microservice.webutils.io;

import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class HttpServletResponseWriter {

    private HttpServletResponseWriter() {

    }

    public static void write(
            HttpServletResponse response,
            String responseBody
    ) throws IOException {
        write(response, SC_OK, APPLICATION_JSON, responseBody);
    }

    public static void write(
            HttpServletResponse response,
            MediaType mediaType,
            String responseBody
    ) throws IOException {
        write(response, SC_OK, mediaType, responseBody);
    }

    public static void write(
            HttpServletResponse response,
            int responseStatus,
            String responseBody
    ) throws IOException {
        write(response, responseStatus, APPLICATION_JSON, responseBody);
    }

    public static void write(
            HttpServletResponse response,
            int responseStatus,
            MediaType mediaType,
            String responseBody
    ) throws IOException {
        response.setStatus(responseStatus);
        response.setContentType(mediaType.toString());
        response.setContentLength(responseBody.getBytes().length);
        response.getWriter().write(responseBody);
    }

}
