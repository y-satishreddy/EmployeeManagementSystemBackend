package pl.piomin.services.employee.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.piomin.services.employee.entity.OutboxEvent;

import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM OutboxEvent o
            WHERE o.id = :id
            """)
    Optional<OutboxEvent> findByIdForUpdate(
            @Param("id") Long id
    );

    List<OutboxEvent> findByProcessingFalse();
}