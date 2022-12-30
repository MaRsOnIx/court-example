package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.judge.event.JudgeUpdatedEvent;
import eu.great.code.courtexample.judge.view.JudgeView;

import java.util.UUID;

public class JudgeEventHandlerImpl implements JudgeEventHandler {

    private final DataStoreSaver<JudgeView, UUID> judgeDataStoreSaver;

    public JudgeEventHandlerImpl(DataStoreSaver<JudgeView, UUID> judgeDataStoreSaver) {
        this.judgeDataStoreSaver = judgeDataStoreSaver;
    }

    @Override
    public void handle(JudgeUpdatedEvent event){
        judgeDataStoreSaver.save(event.judgeView());
    }

}
