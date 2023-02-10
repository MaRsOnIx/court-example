package eu.great.code.courtexample.reservation;

import java.time.LocalDate;
import java.util.Objects;

public class PeriodDate {

    protected PeriodDate() {}

    private LocalDate beginningDate;
    private LocalDate endDate;

    private PeriodDate(
            LocalDate beginningDate,
            LocalDate endDate){
        this.beginningDate = beginningDate;
        this.endDate = endDate;
    }

    public static PeriodDate of(LocalDate beginningDate, LocalDate endDate) {
        Objects.requireNonNull(beginningDate, "Data początkowa nie może być nullem");
        if(endDate != null && (beginningDate.isEqual(endDate) || beginningDate.isAfter(endDate))){
            throw new IllegalArgumentException("Data początkowa nie może być mniejsza," +
                    " bądź równa dacie końcowej");
        }
        return new PeriodDate(beginningDate, endDate);
    }

    public boolean isBetween(LocalDate date){
        if(date == null){
            return endDate == null;
        }
        return date.isEqual(beginningDate) || (date.isAfter(beginningDate) &&
                (endDate == null || date.isEqual(endDate) || date.isBefore(endDate)));
    }

    public boolean isOverlap(PeriodDate periodDate){
        return !((periodDate.beginningDate.isBefore(beginningDate) &&
                periodDate.endDate != null
                && periodDate.endDate.isBefore(beginningDate)) ||
                (endDate != null && periodDate.beginningDate.isAfter(endDate))
        );
    }

    public LocalDate getBeginningDate() {
        return beginningDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
