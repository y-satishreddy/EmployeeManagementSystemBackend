package pl.piomin.services.employee.service;

import pl.piomin.services.employee.entity.OutboxEvent;

public interface OutboxPublisherService {
    void publish(OutboxEvent event);
}
