package eu.great.code.courtexample.judge;

public interface DataStoreSaver<T, ID> {
    void save(T obj);
    void remove(ID id);
}
