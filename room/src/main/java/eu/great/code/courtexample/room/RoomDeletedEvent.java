package eu.great.code.courtexample.room;

import java.time.Instant;
import java.util.UUID;

public record RoomDeletedEvent(Instant instant, UUID uuid) {
}
