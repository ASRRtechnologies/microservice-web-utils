package nl.asrr.microservice.webutils.authorization;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtParser {

    private final byte[] secretKey;

    public JwtParser(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public ParsedJwt parse(String jwt) {
        var claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
        return new ParsedJwt(claims.getSubject(), parseAuthorities(claims));
    }

    private List<SimpleGrantedAuthority> parseAuthorities(Claims claims) {
        Collection<?> authoritiesMap = claims.get("authorities", Collection.class);

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
