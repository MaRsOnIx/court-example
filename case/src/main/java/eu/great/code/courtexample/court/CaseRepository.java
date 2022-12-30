package eu.great.code.courtexample.court;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

interface CaseRepository extends JpaRepository<CaseInstance, UUID> {
    @Query("from CaseInstance c where c.active = true")
    List<CaseInstance> findActiveCases();
}
