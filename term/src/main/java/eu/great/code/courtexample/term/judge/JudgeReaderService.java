package eu.great.code.courtexample.term.judge;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.great.code.courtexample.judge.DataStoreReader;
import eu.great.code.courtexample.judge.view.JudgeFunction;
import eu.great.code.courtexample.judge.view.JudgeView;
import eu.great.code.courtexample.reservation.PeriodDateTime;
import io.vavr.control.Try;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JudgeReaderService {

    private final ResourceAvailabilityClient resourceAvailabilityClient;
    private final DataStoreReader<JudgeView, UUID> judgeReader;
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    private static final String JUDGE_FUNCTION_CONTEXT = "function";
    private static final String CHAIRPERSON_TERM_CONTEXT = "chairpersonTerm";

    public JudgeReaderService(ResourceAvailabilityClient resourceAvailabilityClient,
                              DataStoreReader<JudgeView, UUID> judgeReader) {
        this.resourceAvailabilityClient = resourceAvailabilityClient;
        this.judgeReader = judgeReader;
    }

    // checks only one context synchronously, this list need not be atomically consistent
    public Collection<JudgeView> getActiveChairpersons() {
        return judgeReader.findAll()
                .stream()
                .filter(JudgeView::active)
                .filter(v -> v.getCurrentFunction().map(f -> f.function() == JudgeFunction.PRZEWODNICZACY_WYDZIALU).orElse(false))
                .sorted(Comparator.comparing(a -> a.fullname().getLastname()))
                .toList();
    }

    public Collection<JudgeView> getActiveJudges() {
        return judgeReader.findAll()
                .stream()
                .filter(JudgeView::active)
                .filter(v -> v.getCurrentFunction().map(f -> f.function() == JudgeFunction.SEDZIA).orElse(false))
                .sorted(Comparator.comparing(a -> a.fullname().getLastname()))
                .toList();
    }

    public Collection<JudgeView> getExactlyActiveChairpersonsInPeriodTime(PeriodDateTime periodDateTime) {
        return judgeReader.findAll()
                .stream()
                .filter(JudgeView::active)
                .filter(v -> v.getCurrentFunction().map(f -> f.function() == JudgeFunction.PRZEWODNICZACY_WYDZIALU).orElse(false))
                .filter(v -> isAvailableInPeriod(v, periodDateTime))
                .sorted(Comparator.comparing(a -> a.fullname().getLastname()))
                .toList();
    }

    public Collection<JudgeView> getAllChairpersons() {
        return judgeReader.findAll()
                .stream()
                .filter(v -> v.getCurrentFunction().map(f -> f.function() == JudgeFunction.PRZEWODNICZACY_WYDZIALU).orElse(false))
                .sorted(Comparator.comparing(a -> a.fullname().getLastname()))
                .toList();
    }

    public Optional<JudgeView> findJudgeByUuid(UUID uuid){
        return judgeReader.find(uuid);
    }

    public boolean isAvailableInPeriod(UUID judgeUuid, PeriodDateTime periodDateTime){

        JudgeView foundJudge = judgeReader.find(judgeUuid)
                .orElseThrow(() -> new NoSuchElementException("Judge does not exist"));
        return foundJudge.active() && isAvailableInPeriod(foundJudge, periodDateTime);
    }

    private boolean isAvailableInPeriod(JudgeView judge, PeriodDateTime periodDateTime){
        return isAvailableInPeriod(judge.judgeUuid(), JudgeReaderService.JUDGE_FUNCTION_CONTEXT, periodDateTime) &&
                isAvailableInPeriod(judge.judgeUuid(), JudgeReaderService.CHAIRPERSON_TERM_CONTEXT, periodDateTime) ;
    }

    private boolean isAvailable(UUID resourceUuid, String context){
        return Try.run(() -> resourceAvailabilityClient.checkAvailability(resourceUuid, context)).isSuccess();
    }

    private boolean isAvailableInPeriod(UUID resourceUuid, String context, PeriodDateTime periodDateTime){
        return Try.run(() -> resourceAvailabilityClient.checkAvailabilityInTime(resourceUuid, context, objectMapper.writeValueAsString(periodDateTime))).isSuccess();
    }



}
