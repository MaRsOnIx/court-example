package eu.great.code.courtexample.court.judge;

import eu.great.code.courtexample.judge.JudgeEventHandler;
import eu.great.code.courtexample.judge.event.JudgeEvent;
import eu.great.code.courtexample.judge.event.JudgeUpdatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class JudgeListener {

    private final JudgeEventHandler judgeEventHandler;

    JudgeListener(JudgeEventHandler judgeEventHandler) {
        this.judgeEventHandler = judgeEventHandler;
    }

    @KafkaListener(topics = "judge")
    void eventListener(JudgeEvent judgeEvent){
        System.out.println(judgeEvent);
        switch (judgeEvent){
            case JudgeUpdatedEvent e ->  judgeEventHandler.handle(e);
        }
    }
}
