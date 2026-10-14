package pl.piomin.services.employee.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.piomin.services.employee.dto.EmployeeTransferEventDTO;
import pl.piomin.services.employee.entity.Employee;
import pl.piomin.services.employee.entity.OutboxEvent;
import pl.piomin.services.employee.repository.EmployeeRepository;
import pl.piomin.services.employee.repository.OutboxEventRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static pl.piomin.services.employee.enums.OnboardingStatus.ACTIVE;

@Service
public class EmployeeTransferCompletionService {

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final EmployeeRepository employeeRepository;
    private final OutboxService outboxService;

    public EmployeeTransferCompletionService(OutboxService outboxService, EmployeeRepository employeeRepository, ObjectMapper objectMapper, OutboxEventRepository outboxEventRepository) {
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
        this.employeeRepository = employeeRepository;
        this.outboxService = outboxService;
    }

    @Transactional
    public OutboxEvent completeTransfer(Employee employee, String reason) {
        EmployeeTransferEventDTO employeeTransferEventDTO = new EmployeeTransferEventDTO(
                employee.getId(), employee.getDepartmentId(),
                employee.getPendingTargetDepartmentId(),
                reason,
                LocalDateTime.now()
        );
        OutboxEvent outboxEvent = outboxService.savingOutboxEvent(employeeTransferEventDTO, "EMPLOYEE_TRANSFERRED", employee.getId());
        outboxEventRepository.save(outboxEvent);
        employee.setDepartmentId(employee.getPendingTargetDepartmentId());
        employee.setOnboardingStatus(ACTIVE);
        employee.setPendingTransferReason(null);
        employee.setPendingTargetDepartmentId(null);
        employeeRepository.save(employee);
        return outboxEvent;
    }
}
