package nl.asrr.microservice.webutils.authorization;

import lombok.extern.log4j.Log4j2;
import nl.asrr.microservice.webutils.amqp.FailableRabbitTemplate;
import nl.asrr.microservice.webutils.amqp.model.auth.PersistentRole;
import nl.asrr.microservice.webutils.executor.GuaranteedExecutor;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public class AmqpRoleHierarchy implements RoleHierarchy {

    private final FailableRabbitTemplate mq;

    private List<PersistentRole> roles;

    public AmqpRoleHierarchy(FailableRabbitTemplate mq) {
        this.mq = mq;
    }

    @PostConstruct
    private void init() {
        this.roles = GuaranteedExecutor.execute(
                () -> mq.sendFailableAndReceiveAsType("auth", "auth.getRoleHierarchy", "")
        );
        log.info("successfully received role hierarchy");
    }

    @Override
    public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
            Collection<? extends GrantedAuthority> authorities
    ) {
        if (authorities == null || authorities.isEmpty()) {
            return AuthorityUtils.NO_AUTHORITIES;
        }

        Set<GrantedAuthority> reachableRoles = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            reachableRoles.addAll(getReachableGrantedAuthorities(authority));
        }
        return reachableRoles;
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
