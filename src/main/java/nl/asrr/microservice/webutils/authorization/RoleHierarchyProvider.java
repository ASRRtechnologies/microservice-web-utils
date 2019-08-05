package nl.asrr.microservice.webutils.authorization;

import nl.asrr.microservice.webutils.amqp.model.auth.PersistentRole;

import java.util.List;

public interface RoleHierarchyProvider {

    List<PersistentRole> getRoles();

}
