package eu.great.code.courtexample.term.sessiondayroom.command;

import eu.great.code.courtexample.reservation.PeriodTime;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AssignTerm(
        @NotNull(message = "Pokój gdzie będzie się odbywać termin musi być zdefiniowany")
        UUID roomUuid,
        @NotNull(message = "Dzień terminy musi być zdefiniowany")
        LocalDate date,
        @NotNull(message = "Okres czasowy musi być zdefiniowany")
        PeriodTime periodTime) {
}
