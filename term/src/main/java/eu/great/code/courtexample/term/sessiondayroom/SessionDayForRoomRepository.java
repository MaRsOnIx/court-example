package eu.great.code.courtexample.term.sessiondayroom;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface SessionDayForRoomRepository extends MongoRepository<SessionDayRoom, String> {
    @Query("{ 'date' :  ?0}")
    List<SessionDayRoom> findAllByDate(LocalDate date);
    @Query("{ 'date': { $gte: ?0, $lte: ?1}}")
    List<SessionDayRoom> findAllInPeriod(LocalDate startDate, LocalDate endDate);
    Optional<SessionDayRoom> findByRoomUuidAndDate(UUID roomUuid, LocalDate date);
    @Query("{'terms.?0': {'$exists': true}}")
    Optional<SessionDayRoom> findByContainingTerm(UUID termUuid);

    @Query("{ 'date' :  ?0, roomUuid :  ?1}")
    Optional<SessionDayRoom> findByDateAndRoomUuid(LocalDate date, UUID roomUuid);
}
