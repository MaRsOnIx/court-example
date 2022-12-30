package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.Error;
import eu.great.code.courtexample.Fullname;
import eu.great.code.courtexample.judge.view.JudgeFunction;
import eu.great.code.courtexample.judge.view.JudgeFunctionHistoryView;
import eu.great.code.courtexample.judge.view.JudgeView;
import eu.great.code.courtexample.reservation.Period;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.annotation.PersistenceCreator;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static eu.great.code.courtexample.Error.of;

@Entity
class JudgeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID judgeUuid;
    @Embedded
    private Fullname fullname;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FunctionHistory> functionHistory = new HashSet<>();
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    private boolean active;

    private static final Logger logger = LogManager.getLogger(JudgeEntity.class);


    private JudgeEntity(Fullname fullname, Set<FunctionHistory> functionHistory) {
        this.fullname = fullname;
        this.functionHistory = functionHistory;
        this.active = true;
    }

    static JudgeEntity create(Fullname fullname, JudgeFunction function, LocalDate beginningDate, LocalDate endDate) {
        return new JudgeEntity(fullname, Set.of(new FunctionHistory(function, Period.of(beginningDate, endDate))));
    }

    @PersistenceCreator
    protected JudgeEntity() {
    }

    Either<Error, Void> unactive() {
        if (!active) {
            return Either.left(of("Sędzia jest już nieaktywny"));
        }
        active = false;
        return Either.right(null);
    }

    Either<Error, Void> active() {
        if (active) {
            return Either.left(of("Sędzia jest już aktywny"));
        }
        active = true;
        return Either.right(null);
    }

    Either<Error, Void> updateFullname(Fullname fullname) {
        if (!active) {
            return Either.left(of("Nie jest możliwe zaktualizowanie danych " +
                    "sędziego, ponieważ jest nieaktywny"));
        }
        this.fullname = fullname;
        return Either.right(null);
    }

    private Optional<FunctionHistory> findPreviousFunction() {
        return Optional.ofNullable(functionHistory
                .stream()
                .filter(v -> v.getPeriod().getEndDate() == null)
                .findAny()
                .orElseGet(() -> functionHistory
                        .stream()
                        .max(Comparator.comparing(a -> a.getPeriod().getEndDate()))
                        .orElse(null)));
    }

//    Either<Error, AssignFunctionState> assignFunction(JudgeFunction judgeFunction, LocalDate beginningDate, LocalDate endDate) {
//
//        ErrorContainer errors = new ErrorContainer();
//
//        Try<Period> tryConstructPeriod = Try.of(() -> Period.of(beginningDate, endDate))
//                .onFailure(v -> errors.appendError(v.getMessage()));
//
//        if (tryConstructPeriod.isFailure()) {
//            return Either.left(of(tryConstructPeriod.getCause().getMessage()));
//        }
//
//        Period periodOfNewFunction = tryConstructPeriod.get();
//
//        AssignFunctionState assignFunctionState = checkStateByPeriod(periodOfNewFunction);
//
//        JudgeFunctionOverlappingPolicy judgeFunctionOverlappingPolicy = createPolicyByPeriod(assignFunctionState);
//
//        if (judgeFunctionOverlappingPolicy.anyFunctionPeriodOverlappingPeriod(functionHistory, periodOfNewFunction)) {
//            errors.appendError("W podanym okresie od " + beginningDate + " do " + (endDate == null ? "nieokreślona" : endDate) +
//                    ", sędzia pełni już funkcję: (%s)".formatted(
//                            judgeFunctionOverlappingPolicy.getOverlappingFunctionHistoryByPeriod(functionHistory, periodOfNewFunction).stream()
//                                    .map(FunctionHistory::toReadableString)
//                                    .collect(Collectors.joining(", "))));
//        }
//
//        Optional<FunctionHistory> previousFunctionOfPeriod = findPreviousFunctionOfPeriod(periodOfNewFunction);
//
//        previousFunctionOfPeriod.ifPresent(v -> {
//            if (v.getFunction() == judgeFunction) {
//                errors.appendError("Sędzia nie może pełnić pod rząd tej samej funkcji, poprzednia funkcja: (%s)".formatted(v.toReadableString()));
//            }
//        });
//
//        if (errors.isEmpty()) {
//            previousFunctionOfPeriod.ifPresent(previousFunction -> {
//                PreviousFunctionUpdatePolicy policy = createPreviousFunctionUpdatePolicy(assignFunctionState);
//                if (policy.shouldPreviousFunctionBeUpdated(previousFunction, periodOfNewFunction)) {
//                    Try.of(() -> previousFunction)
//                            .andThenTry(function -> function.updateEndDate(periodOfNewFunction.getBeginningDate().minusDays(1)))
//                            .onFailure(v -> errors.appendError(v.getMessage()))
//                            .onSuccess(v -> logger.info("Funkcja o identyfikatorze %s została zaktualizowana (%s)".formatted(v.functionUuid, v.toReadableString())));
//                }
//            });
//
//            functionHistory.add(new FunctionHistory(judgeFunction, periodOfNewFunction));
//
//            return Either.right(assignFunctionState);
//
//        }
//        return Either.left(errors.get());
//    }


    Optional<FunctionHistory> findPreviousFunctionOfPeriod(Period period) {
        return functionHistory.stream()
                .filter(v -> v.getPeriod().getBeginningDate().isBefore(period.getBeginningDate()))
                .max(Comparator.comparing(v -> v.getPeriod().getBeginningDate()));
    }

    private Optional<FunctionHistory> findNextFunctionOfPeriod(Period period) {
        return functionHistory.stream()
                .filter(v -> v.getPeriod().getBeginningDate().isAfter(period.getEndDate() == null ? period.getBeginningDate() : period.getEndDate()))
                .min(Comparator.comparing(v -> v.getPeriod().getBeginningDate()));
    }

    boolean periodIsNewerThanPeriodOfNewestFunction(Period period) {
        return findPreviousFunction()
                .filter(previousFunction -> previousFunction.getPeriod().getBeginningDate().isAfter(period.getBeginningDate()))
                .isEmpty();
    }

    private List<JudgeFunctionHistoryView> getCurrentAndFutureFunctions(){
        return functionHistory.stream()
                .filter(v -> v.getPeriod().getEndDate() == null || v.getPeriod().getEndDate().isBefore(LocalDate.now()))
                .map(FunctionHistory::toView)
                .toList();
    }


    JudgeView toView() {
        return new JudgeView(judgeUuid, fullname, active, getCurrentAndFutureFunctions());
    }

    List<JudgeFunctionHistoryView> findAllFunctions() {
        return functionHistory
                .stream()
                .map(FunctionHistory::toView)
                .toList();
    }

    Either<Error, Void> assignCurrentFunction(Period period, JudgeFunction function, JudgeFunctionOverlappingPolicy overlappingPolicy) {
        if (overlappingPolicy.anyFunctionPeriodOverlappingPeriod(findAllFunctions(), period)) {
            return constructOverlappingError(period, overlappingPolicy);
        }
        Optional<FunctionHistory> previousFunctionOfPeriod = findPreviousFunctionOfPeriod(period);
        Optional<FunctionHistory> nextFunctionOfPeriod = findNextFunctionOfPeriod(period);

        if(nextFunctionOfPeriod.isPresent()){
            return Either.left(of("Przypisywana funkcja nie może zostać dodana jako obecna, ponieważ istnieje nowsza funkcja"));
        }

        if (previousFunctionOfPeriod.isPresent()) {
            FunctionHistory history = previousFunctionOfPeriod.get();
            if (history.getFunction() == function) {
                return Either.left(of("Sędzia nie może pełnić pod rząd tej samej funkcji, poprzednia funkcja: (%s)".formatted(history.toView().toReadableString())));
            }else {
                Try<FunctionHistory> functionHistories = Try.of(() -> history)
                        .andThenTry(f -> f.updateEndDate(period.getBeginningDate().minusDays(1)))
                        .onSuccess(v -> logger.info("Funkcja o identyfikatorze %s została zaktualizowana (%s)".formatted(v.functionUuid, v.toView().toReadableString())));
                if(functionHistories.isFailure()){
                    return Either.left(of(functionHistories.getCause().getMessage()));
                }
            }
        }
        functionHistory.add(new FunctionHistory(function, period));
        return Either.right(null);
    }

    Either<Error, Void> assignHistoricalFunction(Period period, JudgeFunction function, JudgeFunctionOverlappingPolicy overlappingPolicy) {
        if (overlappingPolicy.anyFunctionPeriodOverlappingPeriod(findAllFunctions(), period)) {
            return constructOverlappingError(period, overlappingPolicy);
        }
        Optional<FunctionHistory> previousFunctionOfPeriod = findPreviousFunctionOfPeriod(period);
        Optional<FunctionHistory> nextFunctionOfPeriod = findNextFunctionOfPeriod(period);

        if(nextFunctionOfPeriod.isEmpty()){
            return Either.left(of("Przypisywana funkcja nie może zostać dodana jako historyczna, ponieważ nowsza funkcja nie istnieje"));
        }
        if (previousFunctionOfPeriod.isPresent()) {
            FunctionHistory history = previousFunctionOfPeriod.get();
            if (history.getFunction() == function) {
                return Either.left(of("Sędzia nie może pełnić pod rząd tej samej funkcji, poprzednia funkcja: (%s)".formatted(history.toView().toReadableString())));
            }
        }
        if(nextFunctionOfPeriod.get().getFunction() == function){
            return Either.left(of("Sędzia nie może pełnić pod rząd tej samej funkcji, późniejsza funkcja: (%s)".formatted(nextFunctionOfPeriod.get().toView().toReadableString())));
        }
        functionHistory.add(new FunctionHistory(function, period));
        return Either.right(null);
    }

    private Either<Error, Void> constructOverlappingError(Period period, JudgeFunctionOverlappingPolicy overlappingPolicy) {
        return Either.left(of("W podanym okresie od " + period.getBeginningDate() + " do " + (period.getEndDate() == null ? "nieokreślona" : period.getEndDate()) +
                ", sędzia pełni już funkcję: (%s)".formatted(
                        overlappingPolicy.getOverlappingFunctionHistoryByPeriod(findAllFunctions(), period).stream()
                                .map(JudgeFunctionHistoryView::toReadableString)
                                .collect(Collectors.joining(", ")))));
    }


    @Entity
    private static class FunctionHistory {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID functionUuid;
        @Enumerated(EnumType.STRING)
        private JudgeFunction function;
        @Embedded
        private Period period;

        private FunctionHistory(JudgeFunction function, Period period) {
            this.function = function;
            this.period = period;
        }

        @PersistenceCreator
        protected FunctionHistory() {
        }

        JudgeFunction getFunction() {
            return function;
        }

        boolean endDateOfFunctionIsDetermined() {
            return period.getEndDate() != null;
        }

        Period getPeriod() {
            return period;
        }

        void updateFunction(JudgeFunction function) {
            this.function = function;
        }

        void updateEndDate(LocalDate endDate) {
            this.period = Period.of(period.getBeginningDate(), endDate);
        }

        void updatePeriod(LocalDate beginningDate, LocalDate endDate) {
            this.period = Period.of(beginningDate, endDate);
        }

        JudgeFunctionHistoryView toView() {
            return new JudgeFunctionHistoryView(function, period);
        }

    }

}
