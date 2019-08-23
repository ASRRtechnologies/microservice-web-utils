package nl.asrr.microservice.webutils.authorization;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Order(1)
@Component
@NoArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {

    private static final String[] TWO_FACTOR_PATHS = {
            "/2fa/generate-qr",
            "/2fa/register",
            "/2fa/validate"
    };

    private FailableRabbitTemplate mq;

    private JwtDetailsProvider jwtProvider;

    private TwoFactorProvider twoFactorProvider;

    /**
     * A secret key to validate the JWT.
     */
    private byte[] jwtSecretKey;

    /**
     * Name of the HTTP header that contains the JWT.
     */
    private String authHeaderName;

    private boolean twoFactorEnabled;

    private String unvalidated2faAuthorityName;

    @Autowired
    public void setMq(FailableRabbitTemplate mq) {
        this.mq = mq;
    }

    public AuthorizationFilter(JwtDetailsProvider jwtProvider, TwoFactorProvider twoFactorProvider) {
        this.jwtProvider = jwtProvider;
        this.twoFactorProvider = twoFactorProvider;
    }

    @PostConstruct
    private void init() {
        if (jwtProvider == null) {
            requestJwtDetails();
        } else {
            this.jwtSecretKey = jwtProvider.getSecretKey();
            this.authHeaderName = jwtProvider.getAuthHeaderName();
        }

        if (twoFactorProvider == null) {
            requestTwoFactorDetails();
        } else {
            this.twoFactorEnabled = twoFactorProvider.enabled();
            this.unvalidated2faAuthorityName = twoFactorProvider.unvalidatedAuthorityName();
        }
    }

    private void requestJwtDetails() {
        byte[] secretKey;
        do {
            log.info("requesting auth.jwt.secretKey ...");
            secretKey = mq.sendFailableAndReceiveAsType("auth", "auth.jwt.secretKey", "");
        } while (secretKey == null);

        String authHeaderName;
        do {
            log.info("requesting auth.jwt.authHeaderName ...");
            authHeaderName = mq.sendFailableAndReceiveAsType("auth", "auth.jwt.authHeaderName", "");
        } while (authHeaderName == null);

        this.jwtSecretKey = secretKey;
        this.authHeaderName = authHeaderName;
        log.info("successfully received jwt details");
    }

    private void requestTwoFactorDetails() {
        Boolean twoFactorEnabled;
        do {
            log.info("requesting auth.2fa.enabled ...");
            twoFactorEnabled = mq.sendFailableAndReceiveAsType("auth", "auth.2fa.enabled", "");
        } while (twoFactorEnabled == null);

        String unvalidated2faAuthorityName;
        do {
            log.info("requesting auth.2fa.unvalidatedAuthorityName ...");
            unvalidated2faAuthorityName = mq.sendFailableAndReceiveAsType("auth", "auth.2fa.unvalidatedAuthorityName", "");
        } while (unvalidated2faAuthorityName == null);

        this.twoFactorEnabled = twoFactorEnabled;
        this.unvalidated2faAuthorityName = unvalidated2faAuthorityName;
        log.info("successfully received two-factor details");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {
        var jwt = request.getHeader(authHeaderName);

        if (Strings.isNullOrEmpty(jwt)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            var claims = parse(jwt);
            var userId = claims.getSubject();
            var authorities = parseAuthorities(claims);

            if (twoFactorEnabled && unvalidated2fa(authorities) && !isTwoFactorPath(request.getServletPath())) {
                writeTokenError(response, "2fa has not been validated");
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(
                    new PreAuthenticatedAuthenticationToken(userId, null, authorities)
            );

            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            writeTokenError(response, "access token is invalid or expired");
        }
    }

    private void writeTokenError(HttpServletResponse response, String message) throws IOException {
        var propertyError = PropertyErrorFactory.of("token", "Invalid", message);
        HttpServletResponseWriter.write(
                response,
                HttpServletResponse.SC_FORBIDDEN,
                propertyError.toPrettyJson()
        );
    }

    private boolean unvalidated2fa(List<SimpleGrantedAuthority> authorities) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(unvalidated2faAuthorityName));
    }

    private boolean isTwoFactorPath(String path) {
        return Arrays.asList(TWO_FACTOR_PATHS).contains(path);
    }

    private Claims parse(String jwt) {
        return Jwts.parser()
                .setSigningKey(jwtSecretKey)
                .parseClaimsJws(jwt)
                .getBody();
    }

    private List<SimpleGrantedAuthority> parseAuthorities(Claims claims) {
        Collection<?> authoritiesMap
                = claims.get("authorities", Collection.class);

        return authoritiesMap.stream().map(
                o -> {
                    if (o instanceof Map) {
                        Object value = ((Map) o).get("authority");
                        if (value instanceof String) {
                            return new SimpleGrantedAuthority((String) value);
                        }
                    }
                    throw new IllegalArgumentException("invalid authority");
                }
        ).collect(Collectors.toList());
    }

}
