package eu.great.code.courtexample.judge;

import eu.great.code.courtexample.judge.view.JudgeFunction;
import eu.great.code.courtexample.judge.view.JudgeFunctionHistoryView;
import eu.great.code.courtexample.reservation.Period;
import io.vavr.control.Either;

import java.util.List;

public interface AssignFunctionPolicy {
    Either<Void, ErrorContainer> assignFunction(JudgeFunction judgeFunction, Period period, List<JudgeFunctionHistoryView> historyElementViews);
}
