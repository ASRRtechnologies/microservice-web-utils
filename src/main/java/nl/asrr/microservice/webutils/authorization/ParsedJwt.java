package nl.asrr.microservice.webutils.authorization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedJwt {

    private String accountId;

    private List<SimpleGrantedAuthority> authorities;

}
