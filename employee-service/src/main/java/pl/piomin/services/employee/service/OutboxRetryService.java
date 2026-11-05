package pl.piomin.services.employee.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.piomin.services.employee.entity.OutboxEvent;
import pl.piomin.services.employee.repository.OutboxEventRepository;

import java.util.List;

@Service
public class OutboxRetryService {

    private static final Logger logger =
            LoggerFactory.getLogger(OutboxRetryService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxPublisherService outboxPublisherService;

    public OutboxRetryService(
            OutboxEventRepository outboxEventRepository,
            OutboxPublisherService outboxPublisherService) {

        this.outboxEventRepository = outboxEventRepository;
        this.outboxPublisherService = outboxPublisherService;
    }

    @Scheduled(fixedDelay = 5000)
    public void retryPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findByProcessingFalse();

        for (OutboxEvent event : events) {

            logger.info(
                    "Retrying OutboxEvent {}",
                    event.getId()
            );

            outboxPublisherService.publish(event);
        }
    }
}