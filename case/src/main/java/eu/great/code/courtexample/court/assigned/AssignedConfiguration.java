package eu.great.code.courtexample.court.assigned;

import eu.great.code.courtexample.court.JudgeAssignedRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Configuration
class AssignedConfiguration {

    private final JudgeAssignedRepository judgeAssignedRepository;

    public AssignedConfiguration(JudgeAssignedRepository judgeAssignedRepository) {
        this.judgeAssignedRepository = judgeAssignedRepository;
    }

    @Bean
    AssignerService counterService(){
        return new AssignerService(Collections.synchronizedMap(judgeAssignedRepository.countJudgesAssigned().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> new AtomicInteger(v.getValue())))));
    }
}
