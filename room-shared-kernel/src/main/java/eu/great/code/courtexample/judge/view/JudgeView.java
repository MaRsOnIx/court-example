package eu.great.code.courtexample.judge.view;

import eu.great.code.courtexample.Fullname;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record JudgeView(
        UUID judgeUuid,
        Fullname fullname,
        boolean active,
        List<JudgeFunctionHistoryView> functionHistory) {

    public Optional<JudgeFunctionHistoryView> getCurrentFunction(){
        return functionHistory.stream()
                .filter(v -> v.period().isBetween(LocalDate.now()))
                .findAny();
    }
}
