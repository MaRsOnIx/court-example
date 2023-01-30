package eu.great.code.courtexample.room;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;

import java.util.UUID;

@Entity
class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID roomUuid;
    @NotEmpty
    private String roomNumber;
    @NotEmpty
    private int floor;
    private String description;

    public UUID getRoomUuid() {
        return roomUuid;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public int getFloor() {
        return floor;
    }

    public String getDescription() {
        return description;
    }
}
