package eu.great.code.courtexample.term.sessiondayroom.query;

import eu.great.code.courtexample.term.sessiondayroom.Containing;
import eu.great.code.courtexample.term.sessiondayroom.LogicalExpression;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record GetDocketsByJudgeInPeriod(
        @NotNull(message = "Należy wskazać datę początkową")
        LocalDate beginningDate,
        LocalDate endDate,
        List<Containing> containing,
        LogicalExpression expression) {
}
