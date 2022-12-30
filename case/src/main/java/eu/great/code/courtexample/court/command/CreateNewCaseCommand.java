package eu.great.code.courtexample.court.command;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateNewCaseCommand(
        @NotEmpty(message = "Przedmiot sprawy nie może być pusty") String przedmiotSprawy,
        @NotEmpty(message = "Symbol sprawy musi być uzupełniony") String symbol,
        @NotNull(message = "Data pierwotnego wpływu musi być uzupełniona") LocalDate dataPierwotnegoWplywu,
        @NotNull(message = "Data wpływu musi być uzupełniona") LocalDate dataWplywu,
        @NotNull(message = "Sędzia referendarz musi być uzupełniony") UUID sedziaReferendarz,
        @NotNull(message = "Sąd musi być uzupełniony")UUID courtUuid
) {
}
