package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.Error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ErrorContainer {
    private final List<Error> errors = new ArrayList<>();

    void appendError(String message) {
        errors.add(Error.of(message));
    }

    List<Error> get() {
        return Collections.unmodifiableList(errors);
    }

    boolean hasErrors() {
        return !errors.isEmpty();
    }

    boolean isEmpty() {
        return errors.isEmpty();
    }
}
