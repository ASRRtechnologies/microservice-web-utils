package nl.asrr.microservice.webutils.authorization;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nl.asrr.microservice.webutils.amqp.FailableRabbitTemplate;
import nl.asrr.microservice.webutils.amqp.model.auth.PersistentRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Log4j2
@Component
@NoArgsConstructor
public class AmqpRoleHierarchy implements RoleHierarchy {

    private FailableRabbitTemplate mq;

    private volatile List<PersistentRole> roles = new ArrayList<>();

    @Autowired
    public void setMq(FailableRabbitTemplate mq) {
        this.mq = mq;
    }

    @PostConstruct
    private void init() {
        Executors.newSingleThreadExecutor().submit(this::requestRoleHierarchy);
    }

    private void requestRoleHierarchy() {
        do {
            log.info("requesting auth.getRoleHierarchy");
            roles = mq.sendFailableAndReceiveAsType("auth", "auth.getRoleHierarchy", "");
        } while (roles == null);

        log.info("successfully received role hierarchy");
    }

    @Override
    public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
            Collection<? extends GrantedAuthority> authorities
    ) {
        if (authorities == null || authorities.isEmpty()) {
            return AuthorityUtils.NO_AUTHORITIES;
        }

        return authorities.stream()
                .flatMap(authority -> getReachableGrantedAuthorities(authority).stream())
                .collect(Collectors.toList());
    }

    private <T extends GrantedAuthority> List<SimpleGrantedAuthority> getReachableGrantedAuthorities(T authority) {
        return roles.stream().filter(r -> r.getRole().equals(authority.getAuthority())).findAny()
                .map(
                        persistentRole -> roles.stream()
                                .filter(r -> r.getPriority() <= persistentRole.getPriority())
                                .map(r -> new SimpleGrantedAuthority(r.getRole()))
                                .collect(Collectors.toList())
                )
                .orElseGet(ArrayList::new);
    }

}
