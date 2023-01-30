package eu.great.code.courtexample.judge.event;

import eu.great.code.courtexample.judge.view.JudgeView;

import java.time.Instant;

public record JudgeUpdatedEvent(Instant instant, JudgeView judgeView) implements JudgeEvent {
}
