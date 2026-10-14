package pl.piomin.services.employee.service;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.piomin.services.employee.client.DepartmentClient;
import pl.piomin.services.employee.dto.DepartmentResponseDTO;
import pl.piomin.services.employee.exception.DepartmentNotFoundException;
import pl.piomin.services.employee.exception.DepartmentServiceNotAvailableException;

@Service
public class DepartmentValidationService {

    private static final Logger log =
            LoggerFactory.getLogger(DepartmentValidationService.class);

    private final DepartmentClient departmentClient;

    public DepartmentValidationService(DepartmentClient departmentClient) {
        this.departmentClient = departmentClient;
    }

    @CircuitBreaker(
            name = "departmentService",
            fallbackMethod = "departmentValidationFallback"
    )
    public DepartmentResponseDTO departmentResponseDTO(Long departmentId) {

        try {

            DepartmentResponseDTO department =
                    departmentClient.getDepartment(departmentId);

            log.info(
                    "Department found: departmentId={}",
                    departmentId
            );

            return department;

        } catch (FeignException.NotFound e) {

            log.warn(
                    "Department not found: departmentId={}",
                    departmentId
            );

            throw new DepartmentNotFoundException(departmentId);
        }
    }

    public DepartmentResponseDTO departmentValidationFallback(
            Long departmentId,
            Throwable throwable) {

        if (throwable instanceof DepartmentNotFoundException) {

            log.warn(
                    "Department does not exist. Skipping fallback. departmentId={}",
                    departmentId
            );

            throw (DepartmentNotFoundException) throwable;
        }
        log.error(
                "Department service failed for departmentId={}",
                departmentId,
                throwable
        );

        throw new DepartmentServiceNotAvailableException(
                "Department service is temporarily unavailable"
        );
    }
}