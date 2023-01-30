package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.judge.view.JudgeFunctionHistoryView;
import eu.great.code.courtexample.reservation.PeriodDate;

import java.util.Comparator;
import java.util.List;

class HistoricalJudgeFunctionOverlappingPolicy implements JudgeFunctionOverlappingPolicy {

    @Override
    public List<JudgeFunctionHistoryView> getOverlappingFunctionHistoryByPeriod(List<JudgeFunctionHistoryView> functions, PeriodDate periodDate) {
        return functions.stream()
                .filter(v -> v.periodDate().isOverlap(periodDate))
                .sorted(Comparator.comparing(v -> v.periodDate().getBeginningDate()))
                .toList();
    }
}
