package eu.great.code.courtexample.term.sessiondayroom.command;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PrepareTerm(
        @NotNull(message = "Identyfikator terminu musi być zdefiniowany")
        UUID termUuid
) {
}
