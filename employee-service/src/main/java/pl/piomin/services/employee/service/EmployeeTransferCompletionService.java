package pl.piomin.services.employee.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.piomin.services.employee.dto.EmployeeTransferEventDTO;
import pl.piomin.services.employee.dto.EmployeeTransferRequestDTO;
import pl.piomin.services.employee.entity.Employee;
import pl.piomin.services.employee.entity.OutboxEvent;
import pl.piomin.services.employee.repository.EmployeeRepository;
import pl.piomin.services.employee.repository.OutboxEventRepository;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static pl.piomin.services.employee.enums.OnboardingStatus.ACTIVE;

@Service
@Slf4j
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
    public OutboxEvent completeTransfer(Employee employee, EmployeeTransferRequestDTO employeeTransferRequestDTO) {
        EmployeeTransferEventDTO employeeTransferEventDTO = new EmployeeTransferEventDTO(
                UUID.randomUUID().toString(),
                employee.getId(), employee.getDepartmentId(),
                employeeTransferRequestDTO.getTargetDepartmentId(),
                employeeTransferRequestDTO.getReason(),
                LocalDateTime.now()
        );
        OutboxEvent outboxEvent = outboxService.savingOutboxEvent(employeeTransferEventDTO, "EMPLOYEE_TRANSFERRED", employee.getId());
        outboxEventRepository.save(outboxEvent);
        employee.setDepartmentId( employeeTransferRequestDTO.getTargetDepartmentId());
        log.info("Target department id: {}", employee.getDepartmentId());
        employee.setOnboardingStatus(ACTIVE);
        employee.setPendingTransferReason(null);
        employee.setPendingTargetDepartmentId(null);
        employeeRepository.save(employee);
        return outboxEvent;
    }
}
