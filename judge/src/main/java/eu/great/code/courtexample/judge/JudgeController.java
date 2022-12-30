package eu.great.code.courtexample.judge;


import eu.great.code.courtexample.Fullname;
import eu.great.code.courtexample.judge.command.AssignFunctionToJudgeCommand;
import eu.great.code.courtexample.judge.command.CreateJudgeCommand;
import eu.great.code.courtexample.judge.command.UpdateJudgeCommand;
import eu.great.code.courtexample.judge.event.JudgeUpdatedEvent;
import eu.great.code.courtexample.judge.view.JudgeFunctionHistoryView;
import eu.great.code.courtexample.judge.view.JudgeView;
import eu.great.code.courtexample.reservation.Period;
import eu.great.code.courtexample.reservation.command.BecomeAvailableAsDefaultCommand;
import eu.great.code.courtexample.reservation.command.BecomeUnavailableAsDefaultCommand;
import eu.great.code.courtexample.reservation.command.PeriodicallyReserveAvailabilityCommand;
import eu.great.code.courtexample.reservation.command.RemoveEachReservationCommand;
import io.vavr.control.Try;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@RequestMapping("/")
@RestController
class JudgeController {

    private final JudgeRepository judgeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String JUDGE_TOPIC = "judge";
    private static final String RESERVATION_TOPIC = "reservation";
    private static final String FUNCTION_CONTEXT = "function";

    JudgeController(JudgeRepository judgeRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.judgeRepository = judgeRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping
    public List<JudgeView> findJudges() {
        return judgeRepository.findAllActiveJudges(Sort
                        .by("fullname.lastname")
                        .descending())
                .stream()
                .map(JudgeEntity::toView)
                .toList();
    }

    @GetMapping("{judgeUuid}")
    public JudgeView findJudge(@PathVariable @NotNull @Valid UUID judgeUuid) {
        return findOrThrow(judgeUuid)
                .toView();
    }

    @GetMapping("function/{judgeUuid}")
    public Collection<JudgeFunctionHistoryView> findAllFunctionsForJudge(@PathVariable @NotNull @Valid UUID judgeUuid) {
        return findOrThrow(judgeUuid)
                .findAllFunctions();
    }


    @PostMapping
    @Transactional
    public void createJudge(@RequestBody @Valid CreateJudgeCommand command) {
        if (Optional.ofNullable(command.endDate()).orElse(LocalDate.MAX).isBefore(command.beginningDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Data rozpoczęcia funkcji nie może być większa niż data jej zakończenia");
        }
        JudgeEntity judgeEntity = JudgeEntity.create(
                Fullname.of(command.firstname(),
                        command.lastname()),
                command.function(),
                command.beginningDate(),
                command.endDate());

        JudgeEntity savedJudge = judgeRepository.save(judgeEntity);
        JudgeFunctionHistoryView judgeHistory = judgeEntity.toView().getCurrentFunction().orElseThrow();
        UUID judgeUUid = savedJudge.toView().judgeUuid();

        kafkaTemplate.send(JUDGE_TOPIC, new JudgeUpdatedEvent(Instant.now(), savedJudge.toView()));

        if (command.endDate() == null) {
            kafkaTemplate.send(RESERVATION_TOPIC, new BecomeAvailableAsDefaultCommand(Instant.now(), judgeUUid, FUNCTION_CONTEXT));
        } else {
            kafkaTemplate.send(RESERVATION_TOPIC, new BecomeUnavailableAsDefaultCommand(Instant.now(), judgeUUid, FUNCTION_CONTEXT));
            kafkaTemplate.send(RESERVATION_TOPIC, new PeriodicallyReserveAvailabilityCommand(
                    Instant.now(),
                    judgeUUid,
                    judgeHistory.beginningDate().atStartOfDay(),
                    judgeHistory.endDate().atTime(23, 59, 59),
                    FUNCTION_CONTEXT)
            );

        }
    }

    @PostMapping("unavailable/{judgeUuid}")
    @Transactional
    public void assignUnavailableToJudge(@PathVariable @NotNull @Valid UUID judgeUuid) {
        JudgeEntity judgeEntity = findOrThrow(judgeUuid);

        judgeEntity.unactive()
                .getOrElseThrow(v -> new ResponseStatusException(HttpStatus.CONFLICT, v.message()));

        kafkaTemplate.send(JUDGE_TOPIC, new JudgeUpdatedEvent(Instant.now(), judgeEntity.toView()));
        kafkaTemplate.send(RESERVATION_TOPIC, new RemoveEachReservationCommand(Instant.now(), judgeUuid, FUNCTION_CONTEXT));
        kafkaTemplate.send(RESERVATION_TOPIC, new BecomeUnavailableAsDefaultCommand(Instant.now(), judgeUuid, FUNCTION_CONTEXT));
    }

    @PostMapping("available/{judgeUuid}")
    @Transactional
    public void assignAvailableToJudge(@PathVariable @NotNull @Valid UUID judgeUuid) {
        JudgeEntity judgeEntity = findOrThrow(judgeUuid);
        JudgeView judgeView = judgeEntity.toView();
        JudgeFunctionHistoryView latestFunction = judgeView.getCurrentFunction().orElseThrow();

        judgeEntity.active()
                .getOrElseThrow(v -> new ResponseStatusException(HttpStatus.CONFLICT, v.message()));

        kafkaTemplate.send(JUDGE_TOPIC, new JudgeUpdatedEvent(Instant.now(), judgeView));
        if (latestFunction.endDate() == null) {
            kafkaTemplate.send(RESERVATION_TOPIC, new BecomeAvailableAsDefaultCommand(Instant.now(), judgeUuid, FUNCTION_CONTEXT));
        } else {
            kafkaTemplate.send(RESERVATION_TOPIC, new BecomeUnavailableAsDefaultCommand(Instant.now(), judgeUuid, FUNCTION_CONTEXT));
            kafkaTemplate.send(RESERVATION_TOPIC, new PeriodicallyReserveAvailabilityCommand(
                    Instant.now(),
                    judgeUuid,
                    latestFunction.beginningDate().atStartOfDay(),
                    latestFunction.endDate().atTime(23, 59, 59),
                    FUNCTION_CONTEXT)
            );

        }
    }

    @PostMapping("function")
    @Transactional
    public void assignFunctionToJudge(@RequestBody @Valid AssignFunctionToJudgeCommand command) {

        Period period = Try.of(() -> Period.of(command.begginingDate(), command.endDate()))
                .getOrElseThrow(v -> new ResponseStatusException(HttpStatus.CONFLICT, v.getMessage()));

        JudgeEntity judgeEntity = findOrThrow(command.judgeUuid());


        if (judgeEntity.periodIsNewerThanPeriodOfNewestFunction(period)) {

            judgeEntity.assignCurrentFunction(period, command.function(), new CurrentJudgeFunctionOverlappingPolicy())
                    .getOrElseThrow(v -> new ResponseStatusException(HttpStatus.CONFLICT, v.message()));

            if (command.endDate() == null) {
                kafkaTemplate.send(RESERVATION_TOPIC, new BecomeAvailableAsDefaultCommand(
                        Instant.now(),
                        judgeEntity.toView().judgeUuid(),
                        FUNCTION_CONTEXT)
                );
            } else {
                kafkaTemplate.send(RESERVATION_TOPIC, new BecomeUnavailableAsDefaultCommand(Instant.now(), judgeEntity.toView().judgeUuid(), FUNCTION_CONTEXT));
                kafkaTemplate.send(RESERVATION_TOPIC, new PeriodicallyReserveAvailabilityCommand(
                        Instant.now(),
                        judgeEntity.toView().judgeUuid(),
                        command.begginingDate().atStartOfDay(),
                        command.endDate().atTime(23, 59, 59),
                        FUNCTION_CONTEXT)
                );
            }
            kafkaTemplate.send(JUDGE_TOPIC, new JudgeUpdatedEvent(Instant.now(), judgeEntity.toView()));
        } else {
            judgeEntity.assignHistoricalFunction(period, command.function(), new HistoricalJudgeFunctionOverlappingPolicy())
                    .getOrElseThrow(v -> new ResponseStatusException(HttpStatus.CONFLICT, v.message()));
        }

    }

    @PatchMapping
    @Transactional
    public void updateJudgeInformation(@RequestBody @Valid UpdateJudgeCommand command) {
        JudgeEntity judgeEntity = findOrThrow(command.judgeUuid());
        Fullname oldFullname = judgeEntity.toView().fullname();
        Fullname newFullname = Fullname.of(
                Optional.ofNullable(command.firstname()).orElse(oldFullname.getFirstname()),
                Optional.ofNullable(command.lastname()).orElse(oldFullname.getLastname())
        );

        judgeEntity.updateFullname(newFullname)
                .getOrElseThrow(v -> new ResponseStatusException(HttpStatus.CONFLICT, v.message()));

        kafkaTemplate.send(JUDGE_TOPIC, new JudgeUpdatedEvent(Instant.now(), judgeEntity.toView()));
    }

    private JudgeEntity findOrThrow(UUID judgeUuid) {
        return judgeRepository.findById(judgeUuid).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.CONFLICT, "Sędzia o podanym identyfikatorze nie istnieje"));
    }

}
