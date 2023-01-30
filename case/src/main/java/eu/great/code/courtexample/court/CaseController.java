package eu.great.code.courtexample.court;

import eu.great.code.courtexample.court.assigned.AssignedUpdater;
import eu.great.code.courtexample.court.command.ChangeJudgeCommand;
import eu.great.code.courtexample.court.command.CreateNewCaseCommand;
import eu.great.code.courtexample.court.command.SendCaseToOtherCourtCommand;
import eu.great.code.courtexample.court.judge.JudgeReaderService;
import eu.great.code.courtexample.judge.view.JudgeView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

@RestController
@RequestMapping("/")
class CaseController {

    private final AssignedUpdater assignedUpdater;
    private final CaseRepository caseRepository;
    private final CourtRepository courtRepository;
    private final JudgeReaderService judgeReaderService;
    private final CaseNumberAutoCounter caseNumberAutoCounter;

    CaseController(AssignedUpdater assignedUpdater,
                   CaseRepository caseRepository,
                   CourtRepository courtRepository,
                   JudgeReaderService judgeReaderService,
                   CaseNumberAutoCounter caseNumberAutoCounter) {
        this.assignedUpdater = assignedUpdater;
        this.caseRepository = caseRepository;
        this.courtRepository = courtRepository;
        this.judgeReaderService = judgeReaderService;
        this.caseNumberAutoCounter = caseNumberAutoCounter;
    }

    @GetMapping("court")
    public Collection<CourtWithDepartmentView> getCourtsWithDepartments(){
        return courtRepository.findAll().stream()
                .map(CourtWithDepartmentInstance::toView)
                .sorted(Comparator.comparing(CourtWithDepartmentView::courtName)
                        .thenComparing(CourtWithDepartmentView::departmentName))
                .toList();
    }

    @GetMapping("case")
    public Collection<CaseSnapshot> getCases(){
        return caseRepository.findActiveCases().stream()
                .map(CaseInstance::getSnapshot)
                .sorted(Comparator.comparing(CaseSnapshot::signature))
                .toList();
    }


    @GetMapping("judges/active")
    public Collection<JudgeView> findActiveJudges() {
        return judgeReaderService.getActiveJudges();
    }

    @GetMapping("judges/active/exactly")
    public Collection<JudgeView> findExactlyActiveJudges() {
        return judgeReaderService.getExactlyActiveJudges();
    }

    @PostMapping("createCase")
    @Transactional
    public void createCase(@Valid @RequestBody CreateNewCaseCommand command) {
        UUID judgeUuid = command.sedziaReferendarz();
        CourtWithDepartmentInstance foundCourt = courtRepository.findById(command.courtUuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono sądu"));
        if (!judgeReaderService.isActive(judgeUuid)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Podany sędzia jest niedostępny");
        }
        CaseInstance newCaseEntity = new CaseInstance(
                judgeUuid,
                foundCourt,
                caseNumberAutoCounter.incrementCounterAndGet(),
                Symbol.fromText(command.symbol()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "")),
                command.przedmiotSprawy(),
                command.dataPierwotnegoWplywu(),
                command.dataWplywu()
        );
        caseRepository.save(newCaseEntity);
        assignedUpdater.addAssigned(judgeUuid);
    }

    @PostMapping("sendCase")
    @Transactional
    public void sendCaseToOtherCourt(@Valid @RequestBody SendCaseToOtherCourtCommand command) {
        CaseInstance foundCaseEntity = caseRepository.findById(command.caseUuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono sprawy"));
        CourtWithDepartmentInstance foundCourt = courtRepository.findById(command.courtUuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono sądu"));
        foundCaseEntity.moveToOtherCourt(foundCourt);
        assignedUpdater.removeAssigned(foundCaseEntity.getSnapshot().lastAssignedJudgeUuid());
    }

    @PostMapping("changeJudge")
    @Transactional
    public void changeJudgeInCase(@Valid @RequestBody ChangeJudgeCommand command) {
        CaseInstance foundCaseEntity = caseRepository.findById(command.caseUuid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono sprawy"));
        if (!judgeReaderService.isActive(command.judgeUuid())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Podany sędzia jest niedostępny");
        }
        UUID previousJudgeUuid = foundCaseEntity.getSnapshot().lastAssignedJudgeUuid();
        foundCaseEntity.changeJudge(command.judgeUuid());
        UUID currentJudgeUuid = foundCaseEntity.getSnapshot().lastAssignedJudgeUuid();
        assignedUpdater.removeAssigned(previousJudgeUuid);
        assignedUpdater.addAssigned(currentJudgeUuid);
    }
}
