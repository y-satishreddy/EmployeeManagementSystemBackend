package pl.piomin.services.employee.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.piomin.services.employee.dto.EmployeeTransferRequestDTO;
import pl.piomin.services.employee.dto.OnboardEmployeeRequestDTO;
import pl.piomin.services.employee.entity.Employee;
import pl.piomin.services.employee.enums.OnboardingStatus;
import pl.piomin.services.employee.service.EmployeeService;

@RestController
@RequestMapping("/employees")
@Slf4j
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public Employee addEmp(@Valid @RequestBody OnboardEmployeeRequestDTO request) {
        return employeeService.onboardEmployee(request);
    }

    @PutMapping("/{employeeId}/department")
    public ResponseEntity<Employee> transferEmployeeToTargetDept(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeTransferRequestDTO employeeTransferRequestDTO) {

        log.info(
                "Employee transfer request received for employeeId: {} and targetDepartmentId: {}",
                employeeId,
                employeeTransferRequestDTO.getTargetDepartmentId()
        );

        Employee employee = employeeService.transferEmployeeToTargetDepartment(
                employeeId,
                employeeTransferRequestDTO
        );

        if (employee.getOnboardingStatus() == OnboardingStatus.PENDING_TRANSFER) {
            return ResponseEntity.accepted().body(employee);
        }

        return ResponseEntity.ok(employee);
    }
}
