package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.judge.view.JudgeFunctionHistoryView;
import eu.great.code.courtexample.reservation.Period;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

class HistoricalJudgeFunctionOverlappingPolicy implements JudgeFunctionOverlappingPolicy {

    @Override
    public List<JudgeFunctionHistoryView> getOverlappingFunctionHistoryByPeriod(List<JudgeFunctionHistoryView> functions, Period period) {
        return functions.stream()
                .filter(v -> v.period().isOverlap(period))
                .sorted(Comparator.comparing(v -> v.period().getBeginningDate()))
                .toList();
    }
}
