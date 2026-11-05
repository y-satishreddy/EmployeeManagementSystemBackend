package pl.piomin.services.employee.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pl.piomin.services.employee.entity.OutboxEvent;

import java.util.Optional;

@Service
@Slf4j
public class OutboxPublisherServiceImpl implements OutboxPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxLockService outboxLockService;

    public OutboxPublisherServiceImpl(
            OutboxLockService outboxLockService,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
        this.outboxLockService = outboxLockService;
    }

    @Override
    public void publish(OutboxEvent outboxEvent) {

        Long eventId = outboxEvent.getId();

        Optional<OutboxEvent> optionalEvent =
                outboxLockService.claimEvent(eventId);

        if (optionalEvent.isEmpty()) {
            log.info("Event {} could not be claimed", eventId);
            return;
        }

        OutboxEvent claimedEvent = optionalEvent.get();

        try {
            String topic = getTopic(claimedEvent.getEventType());
            String key = String.valueOf(claimedEvent.getAggregateId());
            String payload = claimedEvent.getPayload();

            log.info(
                    "Publishing event. eventId={}, eventType={}, topic={}, payload={}",
                    claimedEvent.getId(),
                    claimedEvent.getEventType(),
                    topic,
                    payload
            );

            kafkaTemplate.send(topic, key, payload)
                    .whenComplete((result, exception) -> {

                        if (exception == null) {
                            log.info(
                                    "Event published successfully. eventId={}, topic={}",
                                    claimedEvent.getId(),
                                    topic
                            );

                            try {
                                outboxLockService.deleteEvent(
                                        claimedEvent.getId()
                                );
                            } catch (Exception deletionException) {
                                log.error(
                                        "Failed to delete published outbox event {}. " +
                                                "It may be published again during retry.",
                                        claimedEvent.getId(),
                                        deletionException
                                );
                            }

                        } else {
                            log.error(
                                    "Failed to publish event. eventId={}, topic={}",
                                    claimedEvent.getId(),
                                    topic,
                                    exception
                            );

                            releaseEventSafely(claimedEvent.getId());
                        }
                    });

        } catch (Exception exception) {
            log.error(
                    "Could not initiate publishing for event {}",
                    claimedEvent.getId(),
                    exception
            );

            releaseEventSafely(claimedEvent.getId());
        }
    }

    private String getTopic(String eventType) {
        return switch (eventType) {
            case "EMPLOYEE_ONBOARDED" -> "employee-onboarded";
            case "EMPLOYEE_TRANSFERRED" -> "employee-transferred";
            default -> throw new IllegalArgumentException(
                    "Unsupported event type: " + eventType
            );
        };
    }

    private void releaseEventSafely(Long eventId) {
        try {
            outboxLockService.releaseEvent(eventId);
        } catch (Exception exception) {
            log.error(
                    "Failed to release outbox event {} for retry",
                    eventId,
                    exception
            );
        }
    }
}