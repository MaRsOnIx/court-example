package eu.great.code.courtexample.court.judge;

import eu.great.code.courtexample.judge.DataStoreReader;
import eu.great.code.courtexample.judge.JudgeEventHandler;
import eu.great.code.courtexample.judge.JudgeEventHandlerImpl;
import eu.great.code.courtexample.judge.view.JudgeView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
class JudgeConfiguration {

    @Bean
    DataStoreReader<JudgeView, UUID> judgeCachedDataStore(){
        return JudgeCachedDataStore.INSTANCE;
    }

    @Bean
    JudgeEventHandler judgeEventHandler(){
        return new JudgeEventHandlerImpl(JudgeCachedDataStore.INSTANCE);
    }
}
