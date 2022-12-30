package eu.great.code.courtexample.court.assigned;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

class AssignerService implements AssignedProvider, AssignedUpdater {

    private final Map<UUID, AtomicInteger> data;

    AssignerService(Map<UUID, AtomicInteger> data) {
        this.data = data;
    }

    @Override
    public void addAssigned(UUID judgeUuid) {
        data.putIfAbsent(judgeUuid, new AtomicInteger(0));
        data.get(judgeUuid).incrementAndGet();
    }

    @Override
    public void removeAssigned(UUID judgeUuid) {
        data.putIfAbsent(judgeUuid, new AtomicInteger(0));
        data.get(judgeUuid).decrementAndGet();
    }

    @Override
    public int getAssignedCount(UUID judgeUuid) {
        return data.getOrDefault(judgeUuid, new AtomicInteger(0)).get();
    }
}

