package eu.great.code.courtexample.term.sessiondayroom.query;

import eu.great.code.courtexample.term.sessiondayroom.Containing;
import eu.great.code.courtexample.term.sessiondayroom.LogicalExpression;

import java.util.List;

public record GetDocketByJudgesToday(List<Containing> containing, LogicalExpression expression) {
}
