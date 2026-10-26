package pl.piomin.services.department.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmployeeTransferDltListener {

    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeTransferDltListener.class);

    @KafkaListener(
            topics = "employee-transferred.DLT",
            groupId = "department-dlt-handler"
    )
    public void handleFailedTransfer(String message) {

        logger.error(
                "Employee transfer event sent to DLT. Manual investigation required. Message: {}",
                message
        );
    }
}