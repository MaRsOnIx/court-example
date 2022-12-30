package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.judge.view.JudgeFunctionHistoryView;
import eu.great.code.courtexample.reservation.Period;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

class CurrentJudgeFunctionOverlappingPolicy implements JudgeFunctionOverlappingPolicy {

    @Override
    public List<JudgeFunctionHistoryView> getOverlappingFunctionHistoryByPeriod(List<JudgeFunctionHistoryView> functions, Period period) {
        Predicate<JudgeFunctionHistoryView> determinedOverlapping = v -> v.endDateOfFunctionIsDetermined() &&
                v.period().isOverlap(period);
        Predicate<JudgeFunctionHistoryView> notDeterminedOverlapping = v -> (!v.endDateOfFunctionIsDetermined()) &&
                (v.period().getBeginningDate().isEqual(period.getBeginningDate()) ||
                        v.period().getBeginningDate().isAfter(period.getBeginningDate()));
        return functions.stream()
                .filter(determinedOverlapping.or(notDeterminedOverlapping))
                .sorted(Comparator.comparing(v -> v.period().getBeginningDate()))
                .toList();
    }
}
