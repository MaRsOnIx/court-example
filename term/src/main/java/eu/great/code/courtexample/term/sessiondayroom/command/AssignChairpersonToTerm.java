package eu.great.code.courtexample.term.sessiondayroom.command;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignChairpersonToTerm(
        @NotNull(message = "Identyfikator terminu musi być zdefiniowany")
        UUID termUuid,
        @NotNull(message = "Przewodniczący wydziału musi być zdefiniowany")
        UUID chairperson) {
}
