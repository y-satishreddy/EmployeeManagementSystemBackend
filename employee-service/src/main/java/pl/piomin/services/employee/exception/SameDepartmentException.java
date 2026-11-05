package pl.piomin.services.employee.exception;

public class SameDepartmentException extends RuntimeException{
    public SameDepartmentException(String message){
        super(message);
    }
}
