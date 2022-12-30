package eu.great.code.courtexample.court;

import java.time.LocalDate;
import java.util.UUID;

public record CaseSnapshot(
        UUID caseUuid,
        boolean active,
        String signature,
        UUID lastAssignedJudgeUuid,
        String przedmiotSprawy,
        LocalDate dataPierwotnegoWplywu,
        LocalDate dataWplywu,
        String courtName,
        String departmentName) {
}
