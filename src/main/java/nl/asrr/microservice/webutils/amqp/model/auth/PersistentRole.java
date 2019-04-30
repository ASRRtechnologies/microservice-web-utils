package nl.asrr.microservice.webutils.amqp.model.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PersistentRole {

    private String role;

    private int priority;

    public PersistentRole(String role, int priority) {
        setRole(role);
        this.priority = priority;
    }

    public void setRole(String role) {
        if (!role.startsWith("ROLE_")) {
            throw new IllegalArgumentException("a role must start with \"ROLE_\"");
        }
        this.role = role;
    }

}
