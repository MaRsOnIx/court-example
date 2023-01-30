package eu.great.code.courtexample.room;

import eu.great.code.courtexample.reservation.command.BecomeAvailableAsDefaultCommand;
import eu.great.code.courtexample.reservation.command.BecomeUnavailableAsDefaultCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Transactional
class RoomRepositoryListener extends AbstractRepositoryEventListener<Room> {
    private static final String RESERVATION_TOPIC = "reservation";
    private static final String ROOM_CONTEXT = "room";
    private final Logger logger = LogManager.getLogger(RoomRepositoryListener.class);


    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RoomRepositoryListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    protected void onBeforeCreate(Room entity) {
        kafkaTemplate.executeInTransaction(t -> {
            RoomView roomView = toView(entity);
            t.send(RESERVATION_TOPIC, new BecomeAvailableAsDefaultCommand(Instant.now(), roomView.roomUuid(), ROOM_CONTEXT));
            logger.info(roomView + " created");
            return roomView;
        });
    }

    @Override
    protected void onBeforeDelete(Room entity) {
        kafkaTemplate.executeInTransaction(t -> {
            RoomView roomView = toView(entity);
            t.send(RESERVATION_TOPIC, new BecomeUnavailableAsDefaultCommand(Instant.now(), roomView.roomUuid(), ROOM_CONTEXT));
            logger.info(roomView + " deleted");
            return roomView;
        });
    }

    private RoomView toView(Room entity) {
        return new RoomView(
                entity.getRoomUuid(),
                entity.getRoomNumber(),
                entity.getFloor(),
                entity.getDescription()
        );
    }
}
