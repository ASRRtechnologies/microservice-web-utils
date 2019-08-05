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
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Component
@NoArgsConstructor
public class AmqpRoleHierarchy implements RoleHierarchy {

    private FailableRabbitTemplate mq;

    private List<PersistentRole> roles;

    private RoleHierarchyProvider roleHierarchyProvider;

    @Autowired
    public void setMq(FailableRabbitTemplate mq) {
        this.mq = mq;
    }

    public AmqpRoleHierarchy(RoleHierarchyProvider roleHierarchyProvider) {
        this.roleHierarchyProvider = roleHierarchyProvider;
    }

    @PostConstruct
    private void init() {
        if (roleHierarchyProvider == null) {
            requestRoleHierarchy();
        } else {
            this.roles = roleHierarchyProvider.getRoles();
        }
    }

    private void requestRoleHierarchy() {
        List<PersistentRole> roles;
        do {
            log.info("requesting auth.getRoleHierarchy");
            roles = mq.sendFailableAndReceiveAsType("auth", "auth.getRoleHierarchy", "");
        } while (roles == null);

        this.roles = roles;
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
