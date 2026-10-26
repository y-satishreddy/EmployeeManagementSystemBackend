package pl.piomin.services.department.dto;

import java.time.LocalDateTime;

public class EmployeeTransferEventDTO {

    private String eventId;
    private Long employeeId;
    private Long fromDepartmentId;
    private Long toDepartmentId;
    private String reason;
    private LocalDateTime transferredAt;

    public EmployeeTransferEventDTO() {
    }

    public EmployeeTransferEventDTO(
            String eventId,
            Long employeeId,
            Long fromDepartmentId,
            Long toDepartmentId,
            String reason,
            LocalDateTime transferredAt) {
        this.eventId = eventId;
        this.employeeId = employeeId;
        this.fromDepartmentId = fromDepartmentId;
        this.toDepartmentId = toDepartmentId;
        this.reason = reason;
        this.transferredAt = transferredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getFromDepartmentId() {
        return fromDepartmentId;
    }

    public void setFromDepartmentId(Long fromDepartmentId) {
        this.fromDepartmentId = fromDepartmentId;
    }

    public Long getToDepartmentId() {
        return toDepartmentId;
    }

    public void setToDepartmentId(Long toDepartmentId) {
        this.toDepartmentId = toDepartmentId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(LocalDateTime transferredAt) {
        this.transferredAt = transferredAt;
    }
}