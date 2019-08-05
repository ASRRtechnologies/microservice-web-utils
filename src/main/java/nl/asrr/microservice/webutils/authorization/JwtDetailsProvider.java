package nl.asrr.microservice.webutils.authorization;

public interface JwtDetailsProvider {

    byte[] getSecretKey();

    String getAuthHeaderName();

}
