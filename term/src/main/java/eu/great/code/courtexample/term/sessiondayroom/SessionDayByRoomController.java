package eu.great.code.courtexample.term.sessiondayroom;

import eu.great.code.courtexample.judge.view.JudgeView;
import eu.great.code.courtexample.reservation.PeriodDate;
import eu.great.code.courtexample.reservation.command.PeriodicallyReserveAvailabilityCommand;
import eu.great.code.courtexample.reservation.command.PeriodicallyReserveUnavailabilityCommand;
import eu.great.code.courtexample.term.judge.JudgeReaderService;
import eu.great.code.courtexample.term.sessiondayroom.command.*;
import eu.great.code.courtexample.term.sessiondayroom.query.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
class SessionDayByRoomController {
    private final SessionDayForRoomRepository sessionDayForRoomRepository;
    private final JudgeReaderService judgeReaderService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String RESERVATION_TOPIC = "reservation";
    private static final String CHAIRPERSON_TERM_CONTEXT = "chairpersonTerm";

    SessionDayByRoomController(SessionDayForRoomRepository sessionDayForRoomRepository, JudgeReaderService judgeReaderService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.sessionDayForRoomRepository = sessionDayForRoomRepository;
        this.judgeReaderService = judgeReaderService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("sessionDay/all/today")
    public List<SessionDayRoom.SessionDay> generateSessionDaysToday(GetSessionDaysToday query) {
        return sessionDayForRoomRepository.findAllByDate(LocalDate.now())
                .stream()
                .map(v -> v.generateSessionDay(query.containing(), query.expression()))
                .toList();
    }

    @GetMapping("sessionDay/all/byDate")
    public List<SessionDayRoom.SessionDay> generateSessionDaysByDate(GetSessionDaysByDate query) {
        return sessionDayForRoomRepository.findAllByDate(query.date())
                .stream()
                .map(v -> v.generateSessionDay(query.containing(), query.expression()))
                .toList();
    }

    @GetMapping("sessionDay/all/inPeriod")
    public List<SessionDayRoom.SessionDay> generateAllSessionDaysInPeriod(GetSessionDaysInPeriod query) {
        PeriodDate period = PeriodDate.of(query.beginningDate(), query.endDate());
        return sessionDayForRoomRepository.findAllInPeriod(period.getBeginningDate(), period.getEndDate())
                .stream()
                .map(v -> v.generateSessionDay(query.containing(), query.expression()))
                .toList();
    }

    @GetMapping("sessionDay")
    public SessionDayRoom.SessionDay generateSessionDay(GetSessionDayBySessionDayRoomId query) {
        SessionDayRoom sessionDayRoom = sessionDayForRoomRepository.findByDateAndRoomUuid(query.date(), query.roomUuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono takiego dnia sesyjnego"));
        return sessionDayRoom.generateSessionDay(query.containing(), query.expression());
    }

    @GetMapping("docket/all/today")
    public List<SessionDayRoom.DocketByJudge> generateDocketByJudgesToday(GetDocketByJudgesToday query) {
        return sessionDayForRoomRepository.findAllByDate(LocalDate.now())
                .stream()
                .flatMap(v -> v.findAllDocketsByJudges(query.containing(), query.expression()).stream())
                .toList();
    }

    @GetMapping("docket/all/inPeriod")
    public List<SessionDayRoom.DocketByJudge> generateAllDocketByJudgeInPeriod(GetDocketsByJudgeInPeriod query) {
        PeriodDate period = PeriodDate.of(query.beginningDate(), query.endDate());
        return sessionDayForRoomRepository.findAllInPeriod(period.getBeginningDate(), period.getEndDate())
                .stream()
                .flatMap(v -> v.findAllDocketsByJudges(query.containing(), query.expression()).stream())
                .toList();
    }

    @GetMapping("docket")
    public SessionDayRoom.DocketByJudge generateDocketByJudge(GetDocketByDateAndChairperson query) {
        return sessionDayForRoomRepository.findAllByDate(query.date()).stream()
                .filter(v -> v.containsActiveChairPerson(query.chairpersonUuid()))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono takiej wokandy dla sędziego"))
                .findDocketByJudge(query.chairpersonUuid(), query.containing(), query.expression());
    }

    @PostMapping("term")
    public UUID assignNewTerm(@RequestBody @Valid AssignTerm command) {
        SessionDayRoom sessionDayRoom = sessionDayForRoomRepository.findByRoomUuidAndDate(command.roomUuid(), command.date())
                .orElse(new SessionDayRoom(command.roomUuid(), command.date()));
        UUID uuid = sessionDayRoom.defineNewTerm(command.periodTime());
        sessionDayForRoomRepository.save(sessionDayRoom);
        return uuid;
    }

    @PostMapping("term/chairperson")
    public SessionDayRoom.TermSnapshot assignChairpersonToTerm(@RequestBody @Valid AssignChairpersonToTerm command) {
        SessionDayRoom sessionDayRoom = sessionDayForRoomRepository.findByContainingTerm(command.termUuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Podany termin nie istnieje"));
        SessionDayRoom.TermSnapshot termSnapshot = sessionDayRoom.findTerm(command.termUuid()).orElseThrow();
        JudgeView chairperson = judgeReaderService.findJudgeByUuid(command.chairperson())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Podany sędzia przewodniczący nie istnieje"));
        if (judgeReaderService.isAvailableInPeriod(chairperson.judgeUuid(), termSnapshot.periodTime().toPeriodDateTime(sessionDayRoom.getDate()))) {
            sessionDayRoom.assignChairPersonToTerm(command.termUuid(), chairperson);
            kafkaTemplate.send(RESERVATION_TOPIC, new PeriodicallyReserveUnavailabilityCommand(
                    Instant.now(),
                    chairperson.judgeUuid(),
                    termSnapshot.periodTime().getBeginningDate().atDate(sessionDayRoom.getDate()),
                    termSnapshot.periodTime().getEndDate().atDate(sessionDayRoom.getDate()),
                    CHAIRPERSON_TERM_CONTEXT)
            );
            SessionDayRoom.JudgeChairperson previousChairPerson = termSnapshot.chairPerson();
            if(previousChairPerson != null){
                kafkaTemplate.send(RESERVATION_TOPIC, new PeriodicallyReserveAvailabilityCommand(
                        Instant.now(),
                        chairperson.judgeUuid(),
                        termSnapshot.periodTime().getBeginningDate().atDate(sessionDayRoom.getDate()),
                        termSnapshot.periodTime().getEndDate().atDate(sessionDayRoom.getDate()),
                        CHAIRPERSON_TERM_CONTEXT)
                );
            }
            sessionDayForRoomRepository.save(sessionDayRoom);
            return sessionDayRoom.findTerm(command.termUuid()).orElseThrow();
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Podany sędzia nie jest w tym czasie dostępny");
    }

    @PostMapping("term/judges")
    public SessionDayRoom.TermSnapshot assignJudgesToTerm(@RequestBody @Valid AssignJudgesToTerm command) {
        SessionDayRoom sessionDayRoom = getSessionDayRoomOrThrow(command.termUuid());
        SessionDayRoom.TermSnapshot termSnapshot = sessionDayRoom.findTerm(command.termUuid()).orElseThrow();

        Set<JudgeView> judges = command.judgesUuid().stream()
                .map(v ->judgeReaderService.findJudgeByUuid(v)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Podany sędzia nie istnieje")))
                .collect(Collectors.toUnmodifiableSet());
        sessionDayRoom.assignJudgesToTerm(command.termUuid(), judges);
        sessionDayForRoomRepository.save(sessionDayRoom);

        return termSnapshot;
    }

    @PostMapping("term/prepared")
    public SessionDayRoom.TermSnapshot prepareTerm(@RequestBody @Valid PrepareTerm command){
        SessionDayRoom sessionDayRoom = getSessionDayRoomOrThrow(command.termUuid());
        sessionDayRoom.preparedTerm(command.termUuid());
        sessionDayForRoomRepository.save(sessionDayRoom);
        return sessionDayRoom.findTerm(command.termUuid()).orElseThrow();
    }

    @PostMapping("term/cancel")
    public SessionDayRoom.TermSnapshot cancelTerm(@RequestBody @Valid CancelTerm command){
        SessionDayRoom sessionDayRoom = getSessionDayRoomOrThrow(command.termUuid());
        sessionDayRoom.cancelTerm(command.termUuid());
        sessionDayForRoomRepository.save(sessionDayRoom);
        return sessionDayRoom.findTerm(command.termUuid()).orElseThrow();
    }

    private SessionDayRoom getSessionDayRoomOrThrow(UUID termUuid) {
        return sessionDayForRoomRepository.findByContainingTerm(termUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Podany termin nie istnieje"));
    }


}
