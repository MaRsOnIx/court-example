package eu.great.code.courtexample.room;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.UUID;

@Repository
@RepositoryRestResource(path = "room")
@CrossOrigin
interface RoomRepository extends CrudRepository<Room, UUID> {
}
