package eu.great.code.courtexample.court.assigned;

import java.util.UUID;

public interface AssignedUpdater {
    void addAssigned(UUID judgeUuid);
    void removeAssigned(UUID judgeUuid);
}
