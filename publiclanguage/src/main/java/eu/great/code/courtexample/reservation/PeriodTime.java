package eu.great.code.courtexample.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class PeriodTime {

    protected PeriodTime() {}

    private LocalTime beginningDate;
    private LocalTime endDate;

    private PeriodTime(LocalTime beginningDate, LocalTime endDate){
        this.beginningDate = beginningDate;
        this.endDate = endDate;
    }

    public static PeriodTime of(LocalTime beginningDate, LocalTime endDate) {
        Objects.requireNonNull(beginningDate, "Data początkowa nie może być nullem");
        if(endDate != null && (beginningDate.equals(endDate) || beginningDate.isAfter(endDate))){
            throw new IllegalArgumentException("Data początkowa nie może być mniejsza, bądź równa dacie końcowej");
        }
        return new PeriodTime(beginningDate, endDate);
    }

    public boolean isBetween(LocalTime date){
        if(date == null){
            return endDate == null;
        }
        return date.equals(beginningDate) || (date.isAfter(beginningDate) && (endDate == null || date.equals(endDate) || date.isBefore(endDate)));
    }

    public boolean isOverlap(PeriodTime period){
        return !((period.beginningDate.isBefore(beginningDate) && period.endDate != null && period.endDate.isBefore(beginningDate)) ||
                (endDate != null && period.beginningDate.isAfter(endDate))
        );
    }

    public LocalTime getBeginningDate() {
        return beginningDate;
    }

    public LocalTime getEndDate() {
        return endDate;
    }

    public PeriodDateTime toPeriodDateTime(LocalDate date) {
        return PeriodDateTime.of(date.atTime(beginningDate), date.atTime(endDate));
    }
}
