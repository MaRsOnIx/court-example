package eu.great.code.courtexample.court.judge;

import eu.great.code.courtexample.judge.DataStoreReader;
import eu.great.code.courtexample.judge.DataStoreSaver;
import eu.great.code.courtexample.judge.view.JudgeView;

import java.util.*;

class JudgeCachedDataStore implements DataStoreReader<JudgeView, UUID>, DataStoreSaver<JudgeView, UUID> {

    private final Map<UUID, JudgeView> cached;
    static final JudgeCachedDataStore INSTANCE = new JudgeCachedDataStore();

    private JudgeCachedDataStore() {
        this.cached = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public Optional<JudgeView> find(UUID uuid) {
        return Optional.ofNullable(cached.get(uuid));
    }

    @Override
    public Collection<JudgeView> findAll() {
        return cached.values();
    }

    @Override
    public void save(JudgeView obj) {
        cached.put(obj.judgeUuid(), obj);
    }

    @Override
    public void remove(UUID uuid) {
        cached.remove(uuid);
    }
}
