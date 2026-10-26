package pl.piomin.services.employee.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.piomin.services.employee.dto.DepartmentResponseDTO;
import pl.piomin.services.employee.dto.EmployeeTransferRequestDTO;
import pl.piomin.services.employee.dto.OnboardEmployeeRequestDTO;
import pl.piomin.services.employee.entity.Employee;
import pl.piomin.services.employee.entity.OutboxEvent;
import pl.piomin.services.employee.enums.OnboardingStatus;
import pl.piomin.services.employee.exception.*;
import pl.piomin.services.employee.mapper.EmployeeMapper;
import pl.piomin.services.employee.repository.EmployeeRepository;
import pl.piomin.services.employee.repository.OutboxEventRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static pl.piomin.services.employee.enums.OnboardingStatus.*;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final DepartmentValidationService departmentValidationService;
    private final EmployeeOnboardingCompletionService onboardingCompletionService;
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final OutboxEventRepository outboxEventRepository;
    private ObjectMapper objectMapper;
    private EmployeeTransferCompletionService employeeTransferCompletionService;

    public EmployeeServiceImpl(EmployeeTransferCompletionService employeeTransferCompletionService, DepartmentValidationService departmentValidationService, EmployeeOnboardingCompletionService onboardingCompletionService, EmployeeRepository employeeRepository, EmployeeMapper employeeMapper, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {

        this.departmentValidationService = departmentValidationService;
        this.onboardingCompletionService = onboardingCompletionService;
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.outboxEventRepository = outboxEventRepository;
        this.employeeTransferCompletionService = employeeTransferCompletionService;
    }

    @Transactional
    @Override
    public Employee onboardEmployee(OnboardEmployeeRequestDTO requestDTO) {

        logger.info("Request entered service | name={} | departmentId={} ", requestDTO.getName(), requestDTO.getDepartmentId());

        try {

            // 1. Validate department
            DepartmentResponseDTO departmentResponse = departmentValidationService.departmentResponseDTO(requestDTO.getDepartmentId());

            logger.info("Department validation successful: {}", departmentResponse);

            // 2. Department exists → create ACTIVE employee
            Employee employee = employeeMapper.toEntity(requestDTO);

            employee.setOnboardingStatus(ACTIVE);

            Employee response = employeeRepository.save(employee);

            // 3. Continue onboarding
            return onboardingCompletionService.completeOnboarding(response);

        } catch (DepartmentServiceNotAvailableException exception) {

            // Department service unavailable.
            // Save employee as PENDING and STOP.

            logger.warn("Department service unavailable. " + "Saving employee as PENDING_VALIDATION. departmentId={}", requestDTO.getDepartmentId());

            Employee employee = employeeMapper.toEntity(requestDTO);

            employee.setOnboardingStatus(PENDING_VALIDATION);

            return employeeRepository.save(employee);
        }
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void retryPendingEmployees() {

        List<Employee> pendingEmployees = employeeRepository.findByOnboardingStatus(PENDING_VALIDATION);

        for (Employee employee : pendingEmployees) {

            try {

                // 1. Validate department again
                departmentValidationService.departmentResponseDTO(employee.getDepartmentId());

                // 2. Department is available → activate employee
                employee.setOnboardingStatus(ACTIVE);

                Employee savedEmployee = employeeRepository.save(employee);

                // 3. Continue onboarding
                onboardingCompletionService.completeOnboarding(savedEmployee);

                logger.info("Pending employee onboarding completed. employeeId={}, departmentId={}", employee.getId(), employee.getDepartmentId());

            } catch (DepartmentNotFoundException exception) {

                // Department no longer exists → remove employee

                logger.warn("Department not found. Removing pending employee. " + "employeeId={}, departmentId={}", employee.getId(), employee.getDepartmentId());

                employeeRepository.delete(employee);

            } catch (DepartmentServiceNotAvailableException exception) {

                // Department service is still unavailable.
                // Leave employee as PENDING_VALIDATION.

                logger.warn("Department service still unavailable. " + "Employee remains PENDING_VALIDATION. employeeId={}, departmentId={}", employee.getId(), employee.getDepartmentId());
            }
        }
    }

    @Override
    public Employee transferEmployeeToTargetDepartment(Long employeeId, EmployeeTransferRequestDTO employeeTransferRequestDTO) {
        Employee targetEmployee = employeeRepository.findById(employeeId).orElseThrow(() -> {
            throw new EmployeeNotFoundException("Employee Not Found with Id: " + employeeId);
        });
        log.info("Employee found in the DB with id: {}", employeeId);
        if (targetEmployee.getOnboardingStatus() != OnboardingStatus.ACTIVE) {
            throw new InvalidEmployeeStateException("Employee with ID " + employeeId + " cannot be transferred because their current onboarding status is not ACTIVE.");
        }
        log.info("Employee is ACTIVE");
        if (targetEmployee.getDepartmentId() == employeeTransferRequestDTO.getTargetDepartmentId()) {
            throw new SameDepartmentException("The Employee current department id:" + employeeId + " and employee department id:" + employeeTransferRequestDTO.getTargetDepartmentId() + " are same");
        }
        log.info("Employee Current department id:{} and target department id: {}", targetEmployee.getDepartmentId(), employeeTransferRequestDTO.getTargetDepartmentId());
        try {
            DepartmentResponseDTO departmentResponse = departmentValidationService.departmentResponseDTO(employeeTransferRequestDTO.getTargetDepartmentId());
            logger.info("Department validation successful: {}", departmentResponse);
            OutboxEvent outboxEvent = employeeTransferCompletionService.completeTransfer(targetEmployee, employeeTransferRequestDTO);

        } catch (DepartmentServiceNotAvailableException e) {
            logger.warn("Department service unavailable. Transfer marked as pending for employeeId={}", employeeId);
            targetEmployee.setOnboardingStatus(PENDING_TRANSFER);
            targetEmployee.setPendingTargetDepartmentId(employeeTransferRequestDTO.getTargetDepartmentId());
            targetEmployee.setPendingTransferReason(employeeTransferRequestDTO.getReason());
            employeeRepository.save(targetEmployee);
            return targetEmployee;
        }

        return targetEmployee;
    }

    @Override
    public void retryPendingTransfers() {
        List<Employee> pendingTransferEmployees = employeeRepository.findByOnboardingStatus(PENDING_TRANSFER);
        if (pendingTransferEmployees.isEmpty()) {
            log.info("There are no Pending Employees to transfer");
            return;
        }
        for (Employee employee : pendingTransferEmployees) {
            Long targetDepartmentId = employee.getPendingTargetDepartmentId();
            Long fromDepartmentId = employee.getDepartmentId();
            String reason = employee.getPendingTransferReason();

            try {
                DepartmentResponseDTO departmentResponse = departmentValidationService.departmentResponseDTO(targetDepartmentId);
                EmployeeTransferRequestDTO employeeTransferRequestDTO = new EmployeeTransferRequestDTO(
                        targetDepartmentId, reason
                );
                employeeTransferCompletionService.completeTransfer(employee, employeeTransferRequestDTO);
                log.info("Employee department updated successfully");
            } catch (DepartmentServiceNotAvailableException exception) {
                log.warn(
                        "Department Service is unavailable. Employee remains PENDING_TRANSFER. employeeId={}",
                        employee.getId()
                );
                continue;
            } catch (DepartmentNotFoundException exception) {
                employee.setOnboardingStatus(ACTIVE);
                employee.setPendingTransferReason(null);
                employee.setPendingTargetDepartmentId(null);
                employeeRepository.save(employee);
                log.warn(
                        "Transfer cancelled because target department does not exist. employeeId={}",
                        employee.getId()
                );
                continue;
            }
        }
    }


}
