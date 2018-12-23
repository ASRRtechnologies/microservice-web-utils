package nl.asrr.microservice.webutils.authorization;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import nl.asrr.microservice.webutils.exception.PropertyError;
import nl.asrr.microservice.webutils.io.HttpServletResponseWriter;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Order(1)
public class AuthorizationFilter extends OncePerRequestFilter {

    /**
     * The JWT processor.
     */
    private final JwtProcessor jwtProcessor;

    /**
     * The name of the authorization header.
     */
    private final String authorizationHeader;

    /**
     * Constructs an {@link AuthorizationFilter}.
     *
     * @param jwtProcessor        the JWT processor
     * @param authorizationHeader name of the authorization header
     */
    public AuthorizationFilter(JwtProcessor jwtProcessor, String authorizationHeader) {
        this.jwtProcessor = jwtProcessor;
        this.authorizationHeader = authorizationHeader;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {
        String jwt = request.getHeader(authorizationHeader);

        if (Strings.isNullOrEmpty(jwt)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtProcessor.parse(jwt);
            String userId = claims.getSubject();
            List<SimpleGrantedAuthority> authorities = jwtProcessor.parseAuthorities(claims);

            SecurityContextHolder.getContext().setAuthentication(
                    new PreAuthenticatedAuthenticationToken(userId, null, authorities)
            );

            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            HttpServletResponseWriter.write(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    PropertyError.of("token", "Invalid").toPrettyJson()
            );
        }
    }

}
