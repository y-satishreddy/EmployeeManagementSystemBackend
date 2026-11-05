package pl.piomin.services.department.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.piomin.services.department.entity.DepartmentAudit;

import java.util.Collection;
import java.util.Optional;

public interface DepartmentAuditRepository
        extends JpaRepository<DepartmentAudit, Long> {

    boolean existsByEmployeeId(Long employeeId);

    boolean existsByEmployeeIdAndAction(
            Long employeeId,
            String action
    );

    Optional<DepartmentAudit>
    findTopByEmployeeIdOrderByCreatedAtDescIdDesc(Long employeeId);

    Optional<DepartmentAudit>
    findTopByEmployeeIdAndActionInOrderByCreatedAtDescIdDesc(
            Long employeeId,
            Collection<String> actions
    );

}

