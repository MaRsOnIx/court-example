package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.judge.view.JudgeFunctionHistoryView;
import eu.great.code.courtexample.reservation.Period;

import java.util.List;
import java.util.Set;

interface JudgeFunctionOverlappingPolicy {
    default boolean anyFunctionPeriodOverlappingPeriod(List<JudgeFunctionHistoryView> functions, Period period) {
        return !functions.isEmpty() && !getOverlappingFunctionHistoryByPeriod(functions, period).isEmpty();
    }

    List<JudgeFunctionHistoryView> getOverlappingFunctionHistoryByPeriod(List<JudgeFunctionHistoryView> functions, Period period);
}
