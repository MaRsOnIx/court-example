package eu.great.code.courtexample.judge.event;

import eu.great.code.courtexample.ddd.Event;

public sealed interface JudgeEvent extends Event permits JudgeUpdatedEvent {
}
