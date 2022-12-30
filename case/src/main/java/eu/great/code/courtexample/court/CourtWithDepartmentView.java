package eu.great.code.courtexample.court;

import java.util.UUID;

public record CourtWithDepartmentView(UUID courtUuid, String courtName, String departmentName) {
}
