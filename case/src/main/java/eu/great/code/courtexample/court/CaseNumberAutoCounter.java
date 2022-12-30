package eu.great.code.courtexample.court;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
class CaseNumberAutoCounter {
    private final AtomicInteger counter;

    CaseNumberAutoCounter() {
        this.counter = new AtomicInteger(0);
    }

    synchronized int incrementCounterAndGet() {
        return counter.incrementAndGet();
    }
}
