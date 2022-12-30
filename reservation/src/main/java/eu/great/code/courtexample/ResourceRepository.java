package eu.great.code.courtexample;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
interface ResourceRepository extends JpaRepository<Resource, UUID> {
    @Query("from Resource r where r.resourceUUID = :resourceUUID and r.context = :context")
    Optional<Resource> findResourceByResourceUUIDAndContext(@Param("resourceUUID") UUID resourceUUID, @Param("context") String context);
}
