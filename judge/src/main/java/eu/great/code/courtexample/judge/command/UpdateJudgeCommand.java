package eu.great.code.courtexample.judge.command;

import java.util.UUID;

public record UpdateJudgeCommand(UUID judgeUuid, String firstname, String lastname) {
}
