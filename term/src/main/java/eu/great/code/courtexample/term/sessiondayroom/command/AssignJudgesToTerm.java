package eu.great.code.courtexample.term.sessiondayroom.command;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record AssignJudgesToTerm(
        @NotNull(message = "Identyfikator terminu musi być zdefiniowany")
        UUID termUuid,
        @NotEmpty(message = "Lista sędziów musi być zdefiniowana")
        Set<UUID> judgesUuid) {
}
