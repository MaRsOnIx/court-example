package eu.great.code.courtexample.court.command;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangeJudgeCommand(
        @NotNull(message = "Należy podać identyfikator sprawy") UUID caseUuid,
        @NotNull(message = "Należy podać identyfikator sędziego") UUID judgeUuid
) {
}
