package eu.great.code.courtexample.judge;

import java.util.Collection;
import java.util.Optional;

public interface DataStoreReader<T, ID> {
    Optional<T> find(ID id);
    Collection<T> findAll();
}
