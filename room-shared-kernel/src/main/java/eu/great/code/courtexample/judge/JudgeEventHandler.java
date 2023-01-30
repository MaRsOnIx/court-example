package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.judge.event.JudgeUpdatedEvent;

public interface JudgeEventHandler {
    void handle(JudgeUpdatedEvent event);
}
