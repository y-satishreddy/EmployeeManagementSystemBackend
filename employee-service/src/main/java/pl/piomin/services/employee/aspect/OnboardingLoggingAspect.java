package pl.piomin.services.employee.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.piomin.services.employee.dto.EmployeeTransferRequestDTO;
import pl.piomin.services.employee.entity.Employee;

@Aspect
@Component
public class OnboardingLoggingAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(OnboardingLoggingAspect.class);

    @Around("execution(* pl.piomin.services.employee.service.EmployeeServiceImpl.onboardEmployee(..))")
    public Object logOnboarding(ProceedingJoinPoint joinPoint)
            throws Throwable {

        long startTime = System.currentTimeMillis();

        logger.info(
                "Onboarding started | method={} | args={}",
                joinPoint.getSignature().getName(),
                joinPoint.getArgs()
        );

        try {

            Object result = joinPoint.proceed();

            long executionTime =
                    System.currentTimeMillis() - startTime;

            Employee employee = (Employee) result;

            String outcome =
                    employee.getOnboardingStatus().toString();

            logger.info(
                    "Onboarding completed | method={} | outcome={} | time={}ms",
                    joinPoint.getSignature().getName(),
                    outcome,
                    executionTime
            );

            return result;

        } catch (Throwable exception) {

            long executionTime =
                    System.currentTimeMillis() - startTime;

            logger.error(
                    "Onboarding failed | method={} | time={}ms",
                    joinPoint.getSignature().getName(),
                    executionTime,
                    exception
            );

            throw exception;
        }
    }
    @Around("execution(* pl.piomin.services.employee.service.EmployeeServiceImpl.transferEmployeeToTargetDepartment(..))")
    public Object logEmployeeTransfer(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        Object[] args = joinPoint.getArgs();
        Long employeeId = (Long) args[0];
        EmployeeTransferRequestDTO request = (EmployeeTransferRequestDTO) args[1];

        logger.info(
                "Employee transfer started | employeeId={} | targetDepartmentId={}",
                employeeId,
                request.getTargetDepartmentId()
        );

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            if (result instanceof Employee employee) {
                logger.info(
                        "Employee transfer completed | employeeId={} | departmentId={} | status={} | time={}ms",
                        employee.getId(),
                        employee.getDepartmentId(),
                        employee.getOnboardingStatus(),
                        executionTime
                );
            }

            return result;

        } catch (Throwable exception) {
            long executionTime = System.currentTimeMillis() - startTime;

            logger.error(
                    "Employee transfer failed | employeeId={} | time={}ms",
                    employeeId,
                    executionTime,
                    exception
            );

            throw exception;
        }
    }
}
