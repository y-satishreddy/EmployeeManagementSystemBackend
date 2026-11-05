package pl.piomin.services.employee.exception;

public class InvalidEmployeeStateException extends RuntimeException{
    public InvalidEmployeeStateException(String message) {
        super(message);
    }
}
