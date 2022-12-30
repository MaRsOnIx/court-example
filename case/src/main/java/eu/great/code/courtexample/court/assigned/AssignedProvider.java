package eu.great.code.courtexample.court.assigned;

import java.util.UUID;

public interface AssignedProvider {
    int getAssignedCount(UUID judgeUuid);
}
