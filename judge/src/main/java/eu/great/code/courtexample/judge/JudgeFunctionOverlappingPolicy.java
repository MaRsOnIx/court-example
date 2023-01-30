package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.judge.view.JudgeFunctionHistoryView;
import eu.great.code.courtexample.reservation.PeriodDate;

import java.util.List;

interface JudgeFunctionOverlappingPolicy {
    default boolean anyFunctionPeriodOverlappingPeriod(List<JudgeFunctionHistoryView> functions, PeriodDate periodDate) {
        return !functions.isEmpty() && !getOverlappingFunctionHistoryByPeriod(functions, periodDate).isEmpty();
    }

    List<JudgeFunctionHistoryView> getOverlappingFunctionHistoryByPeriod(List<JudgeFunctionHistoryView> functions, PeriodDate periodDate);
}
