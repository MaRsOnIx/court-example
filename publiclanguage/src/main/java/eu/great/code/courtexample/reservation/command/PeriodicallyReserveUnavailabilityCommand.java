package eu.great.code.courtexample.reservation.command;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record PeriodicallyReserveUnavailabilityCommand(Instant instant, UUID resourceUuid, LocalDateTime startDate, LocalDateTime endDate, String context) implements ReservationCommand {

}
