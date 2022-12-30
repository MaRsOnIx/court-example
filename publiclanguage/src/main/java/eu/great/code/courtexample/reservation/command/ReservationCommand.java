package eu.great.code.courtexample.reservation.command;

import eu.great.code.courtexample.ddd.Command;

public sealed interface ReservationCommand extends Command permits
        BecomeUnavailableAsDefaultCommand,
        BecomeAvailableAsDefaultCommand,
        PeriodicallyReserveAvailabilityCommand,
        PeriodicallyReserveUnavailabilityCommand,
        RemoveEachReservationCommand {
}
