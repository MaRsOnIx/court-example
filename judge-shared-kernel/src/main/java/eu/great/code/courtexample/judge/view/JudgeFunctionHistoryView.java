package eu.great.code.courtexample.judge.view;


import eu.great.code.courtexample.reservation.Period;

import java.time.LocalDate;

public record JudgeFunctionHistoryView(JudgeFunction function, Period period) {

    public LocalDate endDate(){
        return period.getEndDate();
    }

    public LocalDate beginningDate(){
        return period.getBeginningDate();
    }

    public boolean endDateOfFunctionIsDetermined(){
        return endDate() != null;
    }

    public String toReadableString() {
        return "funkcja: " + function.name() + ", okres(%s, %s)"
                .formatted(
                        period.getBeginningDate(),
                        period.getEndDate() == null ? "nieokreślona" : period.getEndDate());
    }
}
