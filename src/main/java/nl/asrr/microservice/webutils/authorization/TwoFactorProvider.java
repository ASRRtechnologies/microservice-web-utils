package nl.asrr.microservice.webutils.authorization;

public interface TwoFactorProvider {

    boolean enabled();

    String unvalidatedAuthorityName();

}
