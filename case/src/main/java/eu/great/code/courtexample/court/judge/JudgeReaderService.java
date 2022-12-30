package eu.great.code.courtexample.court.judge;

import eu.great.code.courtexample.court.assigned.AssignedProvider;
import eu.great.code.courtexample.judge.DataStoreReader;
import eu.great.code.courtexample.judge.view.JudgeFunction;
import eu.great.code.courtexample.judge.view.JudgeView;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JudgeReaderService {

    private final ResourceAvailabilityClient resourceAvailabilityClient;
    private final DataStoreReader<JudgeView, UUID> judgeReader;
    private final AssignedProvider assignedProvider;

    @Value("${possibleAssignedCases}")
    private int possibleAssignedCases;

    private static final String JUDGE_FUNCTION_CONTEXT = "function";
    private static final String CASE_CONTEXT = "case";

    public JudgeReaderService(ResourceAvailabilityClient resourceAvailabilityClient, DataStoreReader<JudgeView, UUID> judgeReader, AssignedProvider assignedProvider) {
        this.resourceAvailabilityClient = resourceAvailabilityClient;
        this.judgeReader = judgeReader;
        this.assignedProvider = assignedProvider;
    }

    // checks only one context synchronously, this list need not be atomically consistent
    public Collection<JudgeView> getActiveJudges() {
        return judgeReader.findAll()
                .stream()
                .filter(JudgeView::active)
                .filter(v -> v.getCurrentFunction().map(f -> f.function() == JudgeFunction.REFERENDARZ).orElse(false))
                .filter(v -> assignedProvider.getAssignedCount(v.judgeUuid()) < possibleAssignedCases)
                .sorted(Comparator.comparing(a -> a.fullname().getLastname()))
                .toList();
    }

    public Collection<JudgeView> getExactlyActiveJudges() {
        return judgeReader.findAll()
                .stream()
                .filter(JudgeView::active)
                .filter(v -> v.getCurrentFunction().map(f -> f.function() == JudgeFunction.REFERENDARZ).orElse(false))
                .filter(v -> assignedProvider.getAssignedCount(v.judgeUuid()) < possibleAssignedCases)
                .filter(this::isAvailable)
                .sorted(Comparator.comparing(a -> a.fullname().getLastname()))
                .toList();
    }

    public Collection<JudgeView> getAllJudges() {
        return judgeReader.findAll()
                .stream()
                .filter(v -> v.getCurrentFunction().map(f -> f.function() == JudgeFunction.REFERENDARZ).orElse(false))
                .sorted(Comparator.comparing(a -> a.fullname().getLastname()))
                .toList();
    }

    public Optional<JudgeView> findJudgeByUuid(UUID uuid){
        return judgeReader.find(uuid);
    }

    public boolean isActive(UUID judgeUuid){
        JudgeView foundJudge = judgeReader.find(judgeUuid)
                .orElseThrow(() -> new NoSuchElementException("Judge does not exist"));
        return foundJudge.active() &&
                foundJudge.getCurrentFunction().map(f -> f.function() == JudgeFunction.REFERENDARZ).orElse(false) &&
                isAvailable(foundJudge) &&
                assignedProvider.getAssignedCount(judgeUuid) < possibleAssignedCases;
    }

    private boolean isAvailable(JudgeView judge){
        return isAvailable(judge.judgeUuid(), JudgeReaderService.JUDGE_FUNCTION_CONTEXT);
    }

    private boolean isAvailable(UUID resourceUuid, String context){
        return Try.run(() -> resourceAvailabilityClient.checkAvailability(resourceUuid, context)).isSuccess();
    }



}
