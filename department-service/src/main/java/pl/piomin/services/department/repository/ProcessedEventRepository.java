package pl.piomin.services.department.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.piomin.services.department.entity.ProcessedEvents;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEvents, Long> {

    boolean existsByEventId(String eventId);
}