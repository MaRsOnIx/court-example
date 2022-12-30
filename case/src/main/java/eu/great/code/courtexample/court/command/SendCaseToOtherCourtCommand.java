package eu.great.code.courtexample.court.command;

import java.util.UUID;

public record SendCaseToOtherCourtCommand(UUID caseUuid, UUID courtUuid) {
}
