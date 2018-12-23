package nl.asrr.microservice.webutils.authorization;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtProcessor {

    /**
     * A secret key to validate the JWT.
     */
    private final String secretKey;

    /**
     * The duration of the JWT (in days).The JWT will expire after this duration.
     */
    private final int tokenExpirationDuration;

    /**
     * Constructs a {@link JwtProcessor}.
     *
     * @param secretKey               a secret key to validate the JWT
     * @param tokenExpirationDuration The duration of the JWT (in days).The JWT will expire after
     *                                this duration.
     */
    public JwtProcessor(String secretKey, int tokenExpirationDuration) {
        this.secretKey = secretKey;
        this.tokenExpirationDuration = tokenExpirationDuration;
    }

    public String generate(
            String accountId,
            Collection<SimpleGrantedAuthority> authorities
    ) {
        long expirationDate = LocalDateTime.now().plusWeeks(tokenExpirationDuration)
                .toInstant(ZoneOffset.UTC).toEpochMilli();

        return Jwts.builder()
                .setSubject(accountId)
                .addClaims(Collections.singletonMap("authorities", authorities))
                .setExpiration(new Date(expirationDate))
                .signWith(SignatureAlgorithm.HS512, secretKey.getBytes())
                .compact();
    }

    public Claims parse(String jwt) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(jwt)
                .getBody();
    }

    public List<SimpleGrantedAuthority> parseAuthorities(Claims claims) {
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
