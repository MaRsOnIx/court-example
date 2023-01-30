package eu.great.code.courtexample.term.sessiondayroom.query;

import eu.great.code.courtexample.term.sessiondayroom.Containing;
import eu.great.code.courtexample.term.sessiondayroom.LogicalExpression;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GetSessionDayBySessionDayRoomId(
        UUID roomUuid,
        LocalDate date,
        List<Containing> containing,
        LogicalExpression expression) {
}
