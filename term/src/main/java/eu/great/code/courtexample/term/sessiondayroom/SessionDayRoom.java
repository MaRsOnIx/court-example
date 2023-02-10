package eu.great.code.courtexample.term.sessiondayroom;

import eu.great.code.courtexample.Fullname;
import eu.great.code.courtexample.judge.view.JudgeFunction;
import eu.great.code.courtexample.judge.view.JudgeFunctionHistoryView;
import eu.great.code.courtexample.judge.view.JudgeView;
import eu.great.code.courtexample.reservation.PeriodDateTime;
import eu.great.code.courtexample.reservation.PeriodTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Document
class SessionDayRoom {
    @Id
    private String dayPlanId;
    private UUID roomUuid;
    private LocalDate date;
    private Map<UUID, Term> terms;

    public SessionDayRoom(UUID roomUuid, LocalDate date) {
        this.roomUuid = roomUuid;
        this.date = date;
        this.terms = new HashMap<>();
    }

    protected SessionDayRoom() {}

    public SessionDayRoomId getId() {
        return new SessionDayRoomId(dayPlanId);
    }

    UUID defineNewTerm(PeriodTime periodTime){
        if(isOverlappingWithAnyTerm(periodTime)){
            throw new IllegalStateException("Godziny: %s i %s nakładają się w tym samym czasie z innym terminem"
                    .formatted(periodTime.getBeginningDate(), periodTime.getEndDate()));
        }
        PeriodDateTime periodDateTime = PeriodDateTime.of(
                date.atTime(periodTime.getBeginningDate()),
                date.atTime(periodTime.getEndDate()));
        Term term = new Term(periodDateTime);
        terms.put(term.getSnapshot().termUuid(), term);
        return term.getSnapshot().termUuid();
    }

    void assignJudgesToTerm(UUID termUuid, Collection<JudgeView> judges){
        Term term = getTermOrThrow(termUuid);
        term.assignJudges(judges);
    }

    void assignChairPersonToTerm(UUID termUuid, JudgeView chairPerson){
        Term term = getTermOrThrow(termUuid);
        term.assignChairPerson(chairPerson);
    }

    void cancelTerm(UUID termUuid){
        Term term = getTermOrThrow(termUuid);
        term.cancel();
    }

    void preparedTerm(UUID termUuid){
        Term term = getTermOrThrow(termUuid);
        term.prepared();
    }

    void cancelSessionDay(){
        terms.values().forEach(Term::cancel);
    }

    void cancelTermsBetween(PeriodTime periodTime){
        terms.values().stream()
                .filter(v -> periodTime.isBetween(v.getSnapshot().periodTime().getBeginningDate())
                        && periodTime.isBetween(v.getSnapshot().periodTime().getEndDate()))
                .forEach(Term::cancel);
    }

    void cancelDocketForJudge(JudgeView judge){
        terms.values().stream()
                .filter(v -> v.getSnapshot().chairPerson().judgeUuid().equals(judge.judgeUuid()))
                .forEach(Term::cancel);
    }

    Collection<TermSnapshot> findAllTerms(){
        return terms.values().stream()
                .map(Term::getSnapshot)
                .toList();
    }

    Collection<DocketByJudge> generateDocket(UUID judgeUuid){
        return generateDocketByJudge(judgeUuid, findAllTerms());
    }

    SessionDay generateSessionDay(List<Containing> containing, LogicalExpression logicalExpression){
        List<TermSnapshot> termSnapshots = findFilteredTerms(containing, logicalExpression);
        return generateSessionDay(termSnapshots);
    }

    SessionDay generateSessionDay(){
        return generateSessionDay(findAllTerms());
    }

    private boolean isOverlappingWithAnyTerm(PeriodTime periodTime){
        for (Term anyTerm : terms.values()) {
            if(anyTerm.isCoincidesWithPeriodTime(periodTime)){
                return true;
            }
        }
        return false;
    }

    private List<TermSnapshot> findFilteredTerms(List<Containing> containing, LogicalExpression logicalExpression) {
        if(containing.isEmpty()){
            return Collections.emptyList();
        }
        Predicate<TermSnapshot> predicate = null;
        for (Containing value : Containing.values()) {
            if(containing.contains(value)){
                if(predicate == null){
                    predicate = value.getPredicate();
                    continue;
                }
                if(logicalExpression == LogicalExpression.AND){
                    predicate = predicate.and(value.getPredicate());
                }else{
                    predicate = predicate.or(value.getPredicate());
                }
            }else{
                if(predicate == null){
                    predicate = value.getPredicate();
                    continue;
                }
                if(logicalExpression == LogicalExpression.AND){
                    predicate = predicate.and(value.getPredicate().negate());
                }else{
                    predicate = predicate.or(value.getPredicate().negate());
                }
            }
        }

        for (Containing value : Containing.values()) {
            if(!containing.contains(value)){
                if(logicalExpression == LogicalExpression.AND) {
                    predicate = predicate.and(value.getPredicate().negate());
                }else{
                    predicate = predicate.or(value.getPredicate().negate());
                }
            }
        }
        return findAllTerms().stream().filter(predicate).toList();
    }

    Collection<DocketByJudge> findDocketByJudge(UUID judgeUuid, List<Containing> containing, LogicalExpression logicalExpression){
        List<TermSnapshot> termSnapshots = findFilteredTerms(containing, logicalExpression);
        return generateDocketByJudge(judgeUuid, termSnapshots);
    }

    Collection<DocketByJudge> findAllDocketsByJudges(List<Containing> containing, LogicalExpression logicalExpression){
        List<TermSnapshot> termSnapshots = findFilteredTerms(containing, logicalExpression);
        List<JudgeChairperson> judges = termSnapshots.stream().map(TermSnapshot::chairPerson).distinct().toList();
        return judges.stream().flatMap(v -> generateDocketByJudge(v.judgeUuid(), termSnapshots).stream()).toList();
    }

    private SessionDay generateSessionDay(Collection<TermSnapshot> terms){
        Set<JudgeChairperson> judges = terms.stream()
                .map(TermSnapshot::chairPerson)
                .collect(Collectors.toUnmodifiableSet());

        List<DocketByJudge> docketByJudges = judges.stream()
                .flatMap(v -> generateDocketByJudge(v.judgeUuid(), terms).stream())
                .toList();

        LocalTime dateFrom = docketByJudges.stream()
                .map(DocketByJudge::period)
                .map(PeriodTime::getBeginningDate)
                .min(LocalTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("Brak aktywnych terminów"));

        LocalTime dateUntil = docketByJudges.stream()
                .map(DocketByJudge::period)
                .map(PeriodTime::getEndDate)
                .max(LocalTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("Brak aktywnych terminów"));

        return new SessionDay(dayPlanId, roomUuid, PeriodTime.of(dateFrom, dateUntil), date, docketsAreCancelled(docketByJudges), docketByJudges);
    }

    private Collection<DocketByJudge> generateDocketByJudge(UUID judgeUuid, Collection<TermSnapshot> terms){
        List<TermSnapshot> termsForJudge = terms.stream()
                .filter(v -> v.chairPerson().judgeUuid().equals(judgeUuid))
                .toList();

        if(termsForJudge.isEmpty()){
            throw new NoSuchElementException("Brak aktywnych terminów dla sędziego o identyfikatorze "
                    + judgeUuid);
        }

        JudgeChairperson judge = termsForJudge.stream().findFirst()
                .map(TermSnapshot::chairPerson)
                .orElseThrow();

        List<DocketByJudge> dockets = new ArrayList<>();
        List<TermSnapshot> termSnapshots = terms.stream()
                .sorted(comparing(a -> a.periodTime().getBeginningDate()))
                .filter(v -> v.chairPerson().judgeUuid().equals(judgeUuid))
                .toList();

        for (int i = 0 ; i < termSnapshots.size() ; i++) {
            List<TermSnapshot> termsInDocket = new ArrayList<>();
            TermSnapshot first = termSnapshots.get(i);
            termsInDocket.add(first);
            for (int j = i ; j < termSnapshots.size() ; j++) {
                TermSnapshot next = termSnapshots.get(j);
                if(!first.periodTime().getEndDate().equals(next.periodTime().getBeginningDate())){
                    break;
                }
                termsInDocket.add(next);
                i++;
            }

            LocalTime dateFrom = termsInDocket.stream()
                    .map(TermSnapshot::periodTime)
                    .map(PeriodTime::getBeginningDate)
                    .min(LocalTime::compareTo)
                    .orElseThrow(() -> new IllegalStateException("Brak aktywnych terminów"));

            LocalTime dateUntil = termsInDocket.stream()
                    .map(TermSnapshot::periodTime)
                    .map(PeriodTime::getEndDate)
                    .max(LocalTime::compareTo)
                    .orElseThrow(() -> new IllegalStateException("Brak aktywnych terminów"));

            dockets.add(new DocketByJudge(dayPlanId, roomUuid,
                    PeriodTime.of(dateFrom, dateUntil), date, judge,
                    termsAreCancelled(termSnapshots), termSnapshots));

        }


        return dockets;
    }


    private Term getTermOrThrow(UUID termUuid){
        Term term = terms.get(termUuid);
        if(term == null){
            throw new NoSuchElementException("Nie znaleziono takiego terminu");
        }
        return term;
    }

    private boolean termsAreCancelled(Collection<TermSnapshot> terms){
        return terms.stream().allMatch(TermSnapshot::cancelled);
    }

    private boolean docketsAreCancelled(Collection<DocketByJudge> dockets){
        return dockets.stream().allMatch(DocketByJudge::cancelled);
    }

    public Optional<TermSnapshot> findTerm(UUID termUuid) {
        Term term = terms.get(termUuid);
        if(term == null){
            return Optional.empty();
        }
        return Optional.of(term.getSnapshot());
    }

    LocalDate getDate() {
        return date;
    }

    boolean containsActiveChairPerson(UUID chairpersonUuid) {
        return findFilteredTerms(List.of(Containing.PREPARED), LogicalExpression.AND).stream()
                .anyMatch(v -> v.chairPerson().judgeUuid().equals(chairpersonUuid));
    }


    record SessionDay(String sessionDayRoomId, UUID roomUuid, PeriodTime period, LocalDate date, boolean cancelled, Collection<DocketByJudge> dockets){}
    record DocketByJudge(String sessionDayRoomId, UUID roomUuid, PeriodTime period, LocalDate date, JudgeChairperson judge, boolean cancelled, List<TermSnapshot> terms){}

    record TermSnapshot(UUID termUuid, boolean prepared, boolean cancelled, PeriodTime periodTime, List<Judge> judges, JudgeChairperson chairPerson){}
    record Judge(UUID judgeUuid, Fullname fullname, JudgeFunction function) {}
    record JudgeChairperson(UUID judgeUuid, Fullname fullname) {}


    private static class Term{
        private UUID termUuid;
        private boolean cancelled;
        private boolean prepared;
        private PeriodDateTime periodDateTime;
        private Set<Judge> judges;
        private JudgeChairperson chairPerson;

        private Term(PeriodDateTime periodDateTime) {
            this.termUuid = UUID.randomUUID();
            this.periodDateTime = periodDateTime;
            this.judges = new HashSet<>();
            this.cancelled = false;
            this.prepared = false;
        }

        protected Term(){}

        TermSnapshot getSnapshot(){
            return new TermSnapshot(termUuid, prepared, cancelled,
                    periodDateTime.toTime(), judges.stream().toList(),
                    chairPerson);
        }
        void assignJudges(Collection<JudgeView> judges){
            if(cancelled){
                throw new IllegalStateException("Nie można przypisać sędziów," +
                        " gdyż termin jest odwołany");
            }
            if(prepared){
                throw new IllegalStateException("Nie można przypisać sędziów," +
                        " gdyż termin jest już przygotowany");
            }
            List<Judge> judgesToSave = new ArrayList<>();
            for (JudgeView judge : judges) {
                if (!judge.active()) {
                    throw new IllegalStateException("Każdy sędzia musi być aktywny");
                }
                JudgeFunction functionOnTermDate = getFunctionByTermDateOrThrowError(judge);
                if(functionOnTermDate != JudgeFunction.SEDZIA){
                    throw new IllegalStateException("Sędzia musi pełnić funkcję sędziego");
                }
                judgesToSave.add(new Judge(judge.judgeUuid(), judge.fullname(), functionOnTermDate));
            }
            this.judges.addAll(judgesToSave);
        }

        void assignChairPerson(JudgeView judge){
            if(cancelled){
                throw new IllegalStateException("Nie można przypisać przewodniczącego składu, gdyż termin jest odwołany");
            }
            if(prepared){
                throw new IllegalStateException("Nie można przypisać sędziów, gdyż termin jest już przygotowany");
            }
            JudgeFunction functionOnTermDate = getFunctionByTermDateOrThrowError(judge);
            if(functionOnTermDate != JudgeFunction.PRZEWODNICZACY_WYDZIALU){
                throw new IllegalStateException("Sędzia musi pełnić funkcję przewodniczącego wydziału");
            }
            chairPerson = new JudgeChairperson(judge.judgeUuid(), judge.fullname());
        }

        private JudgeFunction getFunctionByTermDateOrThrowError(JudgeView judge) {
            return judge.functionHistory()
                    .stream()
                    .filter(v -> (v.beginningDate().isBefore(periodDateTime.getBeginningDate().toLocalDate())) || v.beginningDate().isEqual(periodDateTime.getBeginningDate().toLocalDate())
                            && (v.endDate() == null || v.endDate().isAfter(periodDateTime.getBeginningDate().toLocalDate()) || v.endDate().isEqual(periodDateTime.getBeginningDate().toLocalDate())))
                    .findAny()
                    .map(JudgeFunctionHistoryView::function)
                    .orElseThrow(() -> new IllegalStateException("Sędzia nie pełni żadnej funkcji w czasie trwania terminu"));
        }

        void prepared(){
            if(chairPerson == null){
                throw new IllegalStateException("Przewodniczący składu nie został wybrany");
            }
            this.prepared = true;
        }

        void cancel(){
            if(cancelled){
                throw new IllegalStateException("Termin jest już odwołany");
            }
            this.cancelled = true;
        }


        public boolean isCoincidesWithPeriodTime(PeriodTime otherPeriodTime) {
            PeriodTime periodTime = periodDateTime.toTime();
            return periodTime.isOverlap(otherPeriodTime);
        }
    }



}
