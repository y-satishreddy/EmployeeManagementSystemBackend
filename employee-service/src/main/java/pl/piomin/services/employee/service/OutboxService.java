package pl.piomin.services.employee.service;

import pl.piomin.services.employee.entity.OutboxEvent;
import pl.piomin.services.employee.event.EmployeeOnboardEvent;

public interface OutboxService {
    public OutboxEvent saveOutboxEvent(EmployeeOnboardEvent employeeOnboardEvent);
    public OutboxEvent savingOutboxEvent(Object event,String eventType,Long employeeId);
    }
