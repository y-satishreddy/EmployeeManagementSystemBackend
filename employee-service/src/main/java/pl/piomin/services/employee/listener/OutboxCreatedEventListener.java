package pl.piomin.services.employee.listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.piomin.services.employee.entity.OutboxEvent;
import pl.piomin.services.employee.event.OutboxCreatedEvent;
import pl.piomin.services.employee.service.OutboxPublisherService;

@Component
public class OutboxCreatedEventListener {

    private static final Logger logger =
            LoggerFactory.getLogger(OutboxCreatedEventListener.class);

    private final OutboxPublisherService outboxPublisherService;

    public OutboxCreatedEventListener(
            OutboxPublisherService outboxPublisherService) {

        this.outboxPublisherService = outboxPublisherService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOutboxCreated(OutboxCreatedEvent event) {

        OutboxEvent outboxEvent = event.getOutboxEvent();

        logger.info(
                "Outbox transaction committed. Event ID: {}",
                outboxEvent.getId()
        );

        outboxPublisherService.publish(outboxEvent);
    }}