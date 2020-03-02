package nl.asrr.microservice.webutils.cosmosdb;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.persistence.Id;

@Log4j2
public class CosmosMongoRepositoryImpl<T, ID> implements CosmosMongoRepository<T, ID> {

    private final MongoTemplate mongoTemplate;

    public CosmosMongoRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public boolean cosmosUpdate(T entity) {
        return mongoTemplate.upsert(createQuery(entity), createUpdate(entity), entity.getClass())
                .wasAcknowledged();
    }

    private Update createUpdate(T entity) {
        var update = new Update();
        for (var field : entity.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (field.get(entity) != null) {
                    update.set(field.getName(), field.get(entity));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error(e);
            }
        }
        return update;
    }

    private Query createQuery(T entity) {
        var criteria = new Criteria();
        for (var field : entity.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (field.get(entity) != null) {
                    var id = field.getAnnotation(Id.class);
                    var shardKey = field.getAnnotation(CosmosMongoShardKey.class);
                    if (id != null || shardKey != null) {
                        criteria.and(field.getName()).is(field.get(entity));
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error(e);
            }
        }
        return new Query(criteria);
    }

}
