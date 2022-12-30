package eu.great.code.courtexample.judge.command;

import eu.great.code.courtexample.judge.view.JudgeFunction;

import java.time.LocalDate;
import java.util.UUID;

public record AssignFunctionToJudgeCommand(
        UUID judgeUuid,
        JudgeFunction function,
        LocalDate begginingDate,
        LocalDate endDate) {
}
