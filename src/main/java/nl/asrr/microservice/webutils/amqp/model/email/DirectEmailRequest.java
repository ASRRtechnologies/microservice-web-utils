package nl.asrr.microservice.webutils.amqp.model.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectEmailRequest {

    private String recipient;

    private String subject;

    private String body;

    private List<Attachment> attachments;

}
