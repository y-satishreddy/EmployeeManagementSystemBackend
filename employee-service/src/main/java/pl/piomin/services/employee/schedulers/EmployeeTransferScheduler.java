package pl.piomin.services.employee.schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.piomin.services.employee.service.EmployeeService;

@Component
public class EmployeeTransferScheduler {

    private final EmployeeService employeeService;

    public EmployeeTransferScheduler(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Scheduled(fixedDelay = 10000)
    public void retryPendingTransfers() {
        employeeService.retryPendingTransfers();
    }
}
