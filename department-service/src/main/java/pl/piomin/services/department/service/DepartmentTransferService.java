package pl.piomin.services.department.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.piomin.services.department.dto.EmployeeTransferEventDTO;
import pl.piomin.services.department.entity.Department;
import pl.piomin.services.department.entity.DepartmentAudit;
import pl.piomin.services.department.entity.ProcessedEvents;
import pl.piomin.services.department.repository.DepartmentAuditRepository;
import pl.piomin.services.department.repository.DepartmentRepository;
import pl.piomin.services.department.repository.ProcessedEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentTransferService {

    private static final String EMPLOYEE_ONBOARDED =
            "EMPLOYEE_ONBOARDED";

    private static final String EMPLOYEE_TRANSFERRED_OUT =
            "EMPLOYEE_TRANSFERRED_OUT";

    private static final String EMPLOYEE_TRANSFERRED_IN =
            "EMPLOYEE_TRANSFERRED_IN";

    private static final List<String> CURRENT_DEPARTMENT_ACTIONS =
            List.of(
                    EMPLOYEE_ONBOARDED,
                    EMPLOYEE_TRANSFERRED_IN
            );

    private final DepartmentRepository departmentRepository;
    private final DepartmentAuditRepository departmentAuditRepository;
    private final ProcessedEventRepository processedEventRepository;

    public DepartmentTransferService(
            DepartmentRepository departmentRepository,
            DepartmentAuditRepository departmentAuditRepository,
            ProcessedEventRepository processedEventRepository) {

        this.departmentRepository = departmentRepository;
        this.departmentAuditRepository = departmentAuditRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void processTransfer(EmployeeTransferEventDTO event) {

        // 1. Validate the event.
        validateEvent(event);

        // 2. Ignore events already processed.
        if (processedEventRepository.existsByEventId(
                event.getEventId())) {
            return;
        }

        // 3. Verify that employee onboarding has been processed.
        boolean employeeOnboarded =
                departmentAuditRepository.existsByEmployeeIdAndAction(
                        event.getEmployeeId(),
                        EMPLOYEE_ONBOARDED
                );

        if (!employeeOnboarded) {
            throw new IllegalStateException(
                    "Employee not onboarded in Department Service. "
                            + "Employee ID: " + event.getEmployeeId()
            );
        }

        // 4. Find the latest audit that establishes the current department.
        DepartmentAudit latestDepartmentAudit =
                departmentAuditRepository
                        .findTopByEmployeeIdAndActionInOrderByCreatedAtDescIdDesc(
                                event.getEmployeeId(),
                                CURRENT_DEPARTMENT_ACTIONS
                        )
                        .orElseThrow(() -> new IllegalStateException(
                                "Current department could not be determined "
                                        + "for employee: "
                                        + event.getEmployeeId()
                        ));

        Long currentDepartmentId =
                latestDepartmentAudit.getDepartmentId();

        // 5. Verify that the event's source department is correct.
        if (!currentDepartmentId.equals(
                event.getFromDepartmentId())) {

            throw new IllegalStateException(
                    "Employee's current department is "
                            + currentDepartmentId
                            + ", but the event specifies source department "
                            + event.getFromDepartmentId()
            );
        }

        // 6. Retrieve the source and target departments.
        Department oldDepartment = departmentRepository
                .findById(event.getFromDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Source department not found: "
                                + event.getFromDepartmentId()
                ));

        Department newDepartment = departmentRepository
                .findById(event.getToDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Target department not found: "
                                + event.getToDepartmentId()
                ));

        // 7. Prevent the source headcount from becoming negative.
        if (oldDepartment.getHeadcount() <= 0) {
            throw new IllegalStateException(
                    "Source department headcount cannot be decreased"
            );
        }

        // 8. Update both department headcounts.
        oldDepartment.setHeadcount(
                oldDepartment.getHeadcount() - 1
        );

        newDepartment.setHeadcount(
                newDepartment.getHeadcount() + 1
        );

        departmentRepository.save(oldDepartment);
        departmentRepository.save(newDepartment);

        // 9. Create transfer-out and transfer-in audit records.
        LocalDateTime transferredAt =
                event.getTransferredAt() != null
                        ? event.getTransferredAt()
                        : LocalDateTime.now();

        DepartmentAudit transferOutAudit = new DepartmentAudit(
                event.getFromDepartmentId(),
                event.getEmployeeId(),
                EMPLOYEE_TRANSFERRED_OUT,
                transferredAt
        );

        DepartmentAudit transferInAudit = new DepartmentAudit(
                event.getToDepartmentId(),
                event.getEmployeeId(),
                EMPLOYEE_TRANSFERRED_IN,
                transferredAt
        );

        departmentAuditRepository.save(transferOutAudit);
        departmentAuditRepository.save(transferInAudit);

        // 10. Record the event as processed.
        processedEventRepository.save(
                new ProcessedEvents(
                        event.getEventId(),
                        LocalDateTime.now()
                )
        );
    }

    private void validateEvent(EmployeeTransferEventDTO event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Transfer event must not be null"
            );
        }

        if (event.getEventId() == null
                || event.getEventId().isBlank()) {
            throw new IllegalArgumentException(
                    "Transfer event ID must not be blank"
            );
        }

        if (event.getEmployeeId() == null
                || event.getFromDepartmentId() == null
                || event.getToDepartmentId() == null) {
            throw new IllegalArgumentException(
                    "Employee ID and department IDs are required"
            );
        }

        if (event.getFromDepartmentId()
                .equals(event.getToDepartmentId())) {
            throw new IllegalArgumentException(
                    "Source and target departments must be different"
            );
        }
    }
}

