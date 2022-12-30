package eu.great.code.courtexample.reservation.command;

import java.time.Instant;
import java.util.UUID;

public record RemoveEachReservationCommand(Instant instant, UUID resourceUuid, String context) implements ReservationCommand {

}
