package pl.piomin.services.employee.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import pl.piomin.services.employee.entity.OutboxEvent;
import pl.piomin.services.employee.event.EmployeeOnboardEvent;
import pl.piomin.services.employee.repository.OutboxEventRepository;

import java.time.LocalDateTime;

@Service
public class OutboxServiceImpl implements OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxServiceImpl(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public OutboxEvent saveOutboxEvent(EmployeeOnboardEvent employeeOnboardEvent) {
        try {
            String payload = objectMapper.writeValueAsString(employeeOnboardEvent);

            OutboxEvent outboxEvent = new OutboxEvent(

                    "EMPLOYEE_ONBOARDED",
                    employeeOnboardEvent.getEmployeeId(),
                    payload,
                    LocalDateTime.now()

            );
            outboxEventRepository.save(outboxEvent);
            return outboxEvent;
        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Failed to serialize employee onboarding event",
                    exception
            );
        }

    }

    public OutboxEvent savingOutboxEvent(Object event,String eventType,Long employeeId) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(

                    eventType,
                    employeeId,
                    payload,
                    LocalDateTime.now()

            );
            outboxEventRepository.save(outboxEvent);
            return outboxEvent;
        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Failed to serialize employee onboarding event",
                    exception
            );
        }
    }
}