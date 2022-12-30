package eu.great.code.courtexample.court;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface CourtRepository extends JpaRepository<CourtWithDepartmentInstance, UUID> {
}
