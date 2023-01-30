package eu.great.code.courtexample.term.sessiondayroom;

import eu.great.code.courtexample.term.sessiondayroom.SessionDayRoom.TermSnapshot;

import java.util.function.Predicate;

public enum Containing {
    PREPARED(TermSnapshot::prepared),
    NOT_PREPARED(v -> !v.prepared()),
    CANCELLED(TermSnapshot::cancelled);

    private final Predicate<TermSnapshot> predicate;
    Containing(Predicate<TermSnapshot> predicate) {
        this.predicate = predicate;
    }

    public Predicate<TermSnapshot> getPredicate() {
        return predicate;
    }
}
