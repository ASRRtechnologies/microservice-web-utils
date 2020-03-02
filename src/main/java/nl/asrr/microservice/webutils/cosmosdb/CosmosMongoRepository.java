package nl.asrr.microservice.webutils.cosmosdb;

public interface CosmosMongoRepository<T, ID> {

    boolean cosmosUpdate(T entity);

}
