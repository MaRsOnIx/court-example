package eu.great.code.courtexample.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RepositoryRestResource(path = "room")
interface RoomRepository extends JpaRepository<Room, UUID> {
}
