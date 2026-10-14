package pl.piomin.services.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EmployeeTransferEventDTO {

    private Long employeeId;
    private Long fromDepartmentId;
    private Long toDepartmentId;
    private String reason;
    private LocalDateTime transferredAt;
}