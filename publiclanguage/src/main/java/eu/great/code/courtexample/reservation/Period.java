package eu.great.code.courtexample.reservation;

import java.time.LocalDate;
import java.util.Objects;

public class Period {

    protected Period() {}

    private LocalDate beginningDate;
    private LocalDate endDate;

    private Period(LocalDate beginningDate, LocalDate endDate){
        this.beginningDate = beginningDate;
        this.endDate = endDate;
    }

    public static Period of(LocalDate beginningDate, LocalDate endDate) {
        Objects.requireNonNull(beginningDate, "Data początkowa nie może być nullem");
        if(endDate != null && (beginningDate.isEqual(endDate) || beginningDate.isAfter(endDate))){
            throw new IllegalArgumentException("Data początkowa nie może być mniejsza, bądź równa dacie końcowej");
        }
        return new Period(beginningDate, endDate);
    }

    public boolean isBetween(LocalDate date){
        if(date == null){
            return endDate == null;
        }
        return date.isEqual(beginningDate) || (date.isAfter(beginningDate) && (endDate == null || date.isEqual(endDate) || date.isBefore(endDate)));
    }

    public boolean isOverlap(Period period){
        return !((period.beginningDate.isBefore(beginningDate) && period.endDate != null && period.endDate.isBefore(beginningDate)) ||
                (endDate != null && period.beginningDate.isAfter(endDate))
        );
    }

    public LocalDate getBeginningDate() {
        return beginningDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
