
        package pl.piomin.services.department.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.piomin.services.department.dto.EmployeeTransferEventDTO;
import pl.piomin.services.department.event.EmployeeOnboardEvent;
import pl.piomin.services.department.service.DepartmentOnboardingService;
import pl.piomin.services.department.service.DepartmentTransferService;

        @Component
public class EmployeeEventListener {
    private final DepartmentTransferService departmentTransferService;
    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeEventListener.class);

    private final DepartmentOnboardingService onboardingService;
    private final ObjectMapper objectMapper;

    public EmployeeEventListener(
            DepartmentOnboardingService onboardingService,
            ObjectMapper objectMapper,DepartmentTransferService departmentTransferService) {
this.departmentTransferService = departmentTransferService;
        this.onboardingService = onboardingService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "employee-onboarded",
            groupId = "department-service"
    )
    public void handleOnboardEmployee(String message) {

        try {
            EmployeeOnboardEvent event =
                    objectMapper.readValue(
                            message,
                            EmployeeOnboardEvent.class
                    );

            logger.info(
                    "Received employee onboarding event: {}",
                    event.getName()
            );

            onboardingService.handleEmployeeOnboard(event);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to process employee onboarding event",
                    e
            );
        }
    }

    @KafkaListener(
            topics = "employee-transferred",
            groupId = "department-service"
    )
    public void handleEmployeeTransfer(String message) {

        try {
            EmployeeTransferEventDTO eventDTO =
                    objectMapper.readValue(
                            message,
                            EmployeeTransferEventDTO.class
                    );

            logger.info(
                    "Received employee transfer event: employeeId={}, fromDepartmentId={}, toDepartmentId={}",
                    eventDTO.getEmployeeId(),
                    eventDTO.getFromDepartmentId(),
                    eventDTO.getToDepartmentId()
            );

            departmentTransferService.processTransfer(eventDTO);

            logger.info(
                    "Successfully processed transfer event: employeeId={}",
                    eventDTO.getEmployeeId()
            );

        } catch (Exception e) {
            logger.error("Failed to process employee transfer event", e);
            throw new RuntimeException(
                    "Failed to process employee transfer event",
                    e
            );
        }
    }
}

