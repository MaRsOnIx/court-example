package eu.great.code.courtexample.term.sessiondayroom.query;

import eu.great.code.courtexample.term.sessiondayroom.Containing;
import eu.great.code.courtexample.term.sessiondayroom.LogicalExpression;

import java.time.LocalDate;
import java.util.List;

public record GetSessionDaysByDate(
        List<Containing> containing,
        LogicalExpression expression,
        LocalDate date) {
}
