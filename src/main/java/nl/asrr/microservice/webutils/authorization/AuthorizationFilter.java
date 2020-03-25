package nl.asrr.microservice.webutils.authorization;

import com.google.common.base.Strings;
import io.jsonwebtoken.JwtException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nl.asrr.microservice.webutils.amqp.FailableRabbitTemplate;
import nl.asrr.microservice.webutils.exception.propertyerror.factory.PropertyErrorFactory;
import nl.asrr.microservice.webutils.io.HttpServletResponseWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

@Log4j2
@Order(1)
@Component
@NoArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {

    private static final List<String> TOTP_PATHS = List.of(
            "/totp/generate-qr",
            "/totp/validate"
    );

    private FailableRabbitTemplate mq;

    private volatile JwtParser jwtParser;

    /**
     * Name of the HTTP header that contains the access token.
     */
    private volatile String accessTokenHeader;

    private volatile String unvalidatedTotpAuthority;

    private volatile boolean initialized;

    @Autowired
    public void setMq(FailableRabbitTemplate mq) {
        this.mq = mq;
    }

    @PostConstruct
    public void init() {
        Executors.newSingleThreadExecutor().submit(() -> {
            requestTotpDetails();
            requestAccessTokenCredentials();
            initialized = true;
        });
    }

    private void requestAccessTokenCredentials() {
        do {
            log.info("requesting auth.jwt.accessTokenHeader ...");
            accessTokenHeader = mq.sendFailableAndReceiveAsType("auth", "auth.jwt.accessTokenHeader", "");
        } while (accessTokenHeader == null);

        do {
            log.info("requesting auth.jwt.secretKey ...");
            var secretKey = mq.<byte[]>sendFailableAndReceiveAsType("auth", "auth.jwt.secretKey", "");
            jwtParser = new JwtParser(secretKey);
        } while (jwtParser == null);
        log.info("successfully received jwt credentials");
    }

    private void requestTotpDetails() {
        do {
            log.info("requesting auth.totp.unvalidatedAuthorityName ...");
            unvalidatedTotpAuthority = mq.sendFailableAndReceiveAsType("auth", "auth.totp.unvalidatedAuthorityName", "");
        } while (unvalidatedTotpAuthority == null);
        log.info("successfully received totp details");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {
        if (!initialized) {
            HttpServletResponseWriter.write(
                    response,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "AuthorizationFilter has not been initialized"
            );
            return;
        }

        var accessToken = request.getHeader(accessTokenHeader.toLowerCase());
        if (Strings.isNullOrEmpty(accessToken)) {
            chain.doFilter(request, response);
            return;
        }

        initializeSecurityContext(request, response, chain, accessToken);
    }

    private void initializeSecurityContext(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            String accessToken
    ) throws IOException, ServletException {
        try {
            var jwt = jwtParser.parse(accessToken);

            if (containsUnvalidatedTotp(jwt.getAuthorities()) && notTotpPath(request.getServletPath())) {
                writeTokenError(response, "totp", "totp has not been validated");
                return;
            }

            var authentication = new PreAuthenticatedAuthenticationToken(jwt.getAccountId(), null, jwt.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            writeTokenError(response, "accessToken", "access token is invalid or has expired");
        }
    }

    private boolean containsUnvalidatedTotp(List<SimpleGrantedAuthority> authorities) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(unvalidatedTotpAuthority));
    }

    private boolean notTotpPath(String path) {
        return !TOTP_PATHS.contains(path);
    }

    private void writeTokenError(HttpServletResponse response, String property, String message) throws IOException {
        var propertyError = PropertyErrorFactory.of(property, "Invalid", message);
        HttpServletResponseWriter.write(
                response,
                HttpServletResponse.SC_FORBIDDEN,
                propertyError.toPrettyJson()
        );
    }

}
