package eu.great.code.courtexample.room;

import java.time.Instant;

public record RoomUpdatedEvent(Instant instant, RoomView roomView) {
}
