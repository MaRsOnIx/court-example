package eu.great.code.courtexample.term.judge;

import eu.great.code.courtexample.judge.JudgeEventHandler;
import eu.great.code.courtexample.judge.event.JudgeUpdatedEvent;
import eu.great.code.courtexample.reservation.command.BecomeAvailableAsDefaultCommand;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

class JudgeEventHandlerTermImpl implements JudgeEventHandler {

    private final JudgeCachedDataStore dataStore;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String RESERVATION_TOPIC = "reservation";
    private static final String CHAIRPERSON_TERM_CONTEXT = "chairpersonTerm";

    JudgeEventHandlerTermImpl(JudgeCachedDataStore dataStore, KafkaTemplate<String, Object> kafkaTemplate) {
        this.dataStore = dataStore;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void handle(JudgeUpdatedEvent event){
        if(!dataStore.exists(event.judgeView().judgeUuid())){
            kafkaTemplate.send(RESERVATION_TOPIC, new BecomeAvailableAsDefaultCommand(
                    Instant.now(),
                    event.judgeView().judgeUuid(),
                    CHAIRPERSON_TERM_CONTEXT)
            );
        }
        dataStore.save(event.judgeView());
    }

}
