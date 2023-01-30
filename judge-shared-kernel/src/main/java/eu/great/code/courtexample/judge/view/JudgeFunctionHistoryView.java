package eu.great.code.courtexample.judge.view;


import eu.great.code.courtexample.reservation.PeriodDate;

import java.time.LocalDate;

public record JudgeFunctionHistoryView(JudgeFunction function, PeriodDate periodDate) {

    public LocalDate endDate(){
        return periodDate.getEndDate();
    }

    public LocalDate beginningDate(){
        return periodDate.getBeginningDate();
    }

    public boolean endDateOfFunctionIsDetermined(){
        return endDate() != null;
    }

    public String toReadableString() {
        return "funkcja: " + function.name() + ", okres(%s, %s)"
                .formatted(
                        periodDate.getBeginningDate(),
                        periodDate.getEndDate() == null ? "nieokreślona" : periodDate.getEndDate());
    }
}
