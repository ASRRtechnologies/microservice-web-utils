package nl.asrr.microservice.webutils.amqp.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectAccountEmailRequest {

    private String accountId;

    private String subject;

    private String body;

}
