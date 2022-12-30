package eu.great.code.courtexample.judge;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
interface JudgeRepository extends JpaRepository<JudgeEntity, UUID> {
    @Query("select judge from JudgeEntity judge where judge.active = true")
    List<JudgeEntity> findAllActiveJudges(Sort sort);
}
