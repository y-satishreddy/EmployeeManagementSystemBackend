package pl.piomin.services.employee.exception;

import pl.piomin.services.employee.enums.ErrorCodes;
import pl.piomin.services.employee.model.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private ErrorCodes errorCode;
    private String message;
    private List<ValidationError> errors;

    public ErrorResponse() {
    }

    public ErrorResponse(
            LocalDateTime timestamp,
            int status,
            ErrorCodes errorCode,
            String message,
            List<ValidationError> errors) {

        this.timestamp = timestamp;
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCodes errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
}