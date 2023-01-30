package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.judge.view.JudgeFunctionHistoryView;
import eu.great.code.courtexample.reservation.PeriodDate;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

class CurrentJudgeFunctionOverlappingPolicy implements JudgeFunctionOverlappingPolicy {

    @Override
    public List<JudgeFunctionHistoryView> getOverlappingFunctionHistoryByPeriod(List<JudgeFunctionHistoryView> functions, PeriodDate periodDate) {
        Predicate<JudgeFunctionHistoryView> determinedOverlapping = v -> v.endDateOfFunctionIsDetermined() &&
                v.periodDate().isOverlap(periodDate);
        Predicate<JudgeFunctionHistoryView> notDeterminedOverlapping = v -> (!v.endDateOfFunctionIsDetermined()) &&
                (v.periodDate().getBeginningDate().isEqual(periodDate.getBeginningDate()) ||
                        v.periodDate().getBeginningDate().isAfter(periodDate.getBeginningDate()));
        return functions.stream()
                .filter(determinedOverlapping.or(notDeterminedOverlapping))
                .sorted(Comparator.comparing(v -> v.periodDate().getBeginningDate()))
                .toList();
    }
}
