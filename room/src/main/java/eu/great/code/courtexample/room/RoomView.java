package eu.great.code.courtexample.room;

import java.util.UUID;

public record RoomView(UUID roomUuid, String roomNumber, int floor, String description) {
}
