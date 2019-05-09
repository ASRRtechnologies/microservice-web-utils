package nl.asrr.microservice.webutils.authorization;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.log4j.Log4j2;
import nl.asrr.microservice.webutils.amqp.FailableRabbitTemplate;
import nl.asrr.microservice.webutils.exception.propertyerror.PropertyError;
import nl.asrr.microservice.webutils.exception.propertyerror.factory.PropertyErrorFactory;
import nl.asrr.microservice.webutils.executor.GuaranteedExecutor;
import nl.asrr.microservice.webutils.io.HttpServletResponseWriter;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Order(1)
public class AuthorizationFilter extends OncePerRequestFilter {

    private final FailableRabbitTemplate mq;

    /**
     * A secret key to validate the JWT.
     */
    private byte[] secretKey;

    /**
     * Name of the HTTP header that contains the JWT.
     */
    private String authHeaderName;


    public AuthorizationFilter(FailableRabbitTemplate mq) {
        this.mq = mq;
    }

    @PostConstruct
    private void init() {
        this.secretKey = GuaranteedExecutor.execute(
                () -> mq.sendFailableAndReceiveAsType("auth", "auth.jwt.secretKey", "")
        );
        this.authHeaderName = GuaranteedExecutor.execute(
                () -> mq.sendFailableAndReceiveAsType("auth", "auth.jwt.authHeaderName", "")
        );
        log.info("successfully received auth V# info");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {
        String jwt = request.getHeader(authHeaderName);

        if (Strings.isNullOrEmpty(jwt)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = parse(jwt);
            String userId = claims.getSubject();
            List<SimpleGrantedAuthority> authorities = parseAuthorities(claims);

            SecurityContextHolder.getContext().setAuthentication(
                    new PreAuthenticatedAuthenticationToken(userId, null, authorities)
            );

            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            PropertyError propertyError = PropertyErrorFactory.of(
                    "token",
                    "Invalid",
                    "auth token is invalid or expired"
            );
            HttpServletResponseWriter.write(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    propertyError.toPrettyJson()
            );
        }
    }

    private Claims parse(String jwt) {
        return Jwts.parser()
                .setSigningKey(secretKey)
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
