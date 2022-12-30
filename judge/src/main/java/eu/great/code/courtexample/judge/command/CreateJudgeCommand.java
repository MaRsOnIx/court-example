package eu.great.code.courtexample.judge.command;

import eu.great.code.courtexample.judge.view.JudgeFunction;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateJudgeCommand(
        @NotEmpty(message = "Imię sędziego nie może być puste")
        String firstname,
        @NotEmpty(message = "Nazwisko sędziego nie może być puste")
        String lastname,
        @NotNull(message = "Wskaza musi zostać funkcja sędziego")
        JudgeFunction function,
        @NotNull(message = "Wsazana musi zostać data rozpoczęcia funkcji")
        LocalDate beginningDate,
        LocalDate endDate
) {
}
