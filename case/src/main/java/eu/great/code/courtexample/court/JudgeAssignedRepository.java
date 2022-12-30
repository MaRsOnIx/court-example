package eu.great.code.courtexample.court;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Transactional(readOnly = true)
public interface JudgeAssignedRepository extends JpaRepository<CaseInstance, UUID> {

    @Query("select c.lastAssignedJudgeUuid, count(c) from CaseInstance c where c.active = true group by c.lastAssignedJudgeUuid")
    Map<UUID, Integer> countJudgesAssigned();
}
