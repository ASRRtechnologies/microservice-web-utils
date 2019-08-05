package nl.asrr.microservice.webutils.authorization;

public interface TwoFactorProvider {

    boolean twoFactorEnabled();

    String validatedAuthorityName();

}
