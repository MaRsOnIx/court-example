package eu.great.code.courtexample.reservation;

import java.time.LocalDateTime;
import java.util.Objects;

public class PeriodDateTime {

    protected PeriodDateTime() {}

    private LocalDateTime beginningDate;
    private LocalDateTime endDate;

    private PeriodDateTime(LocalDateTime beginningDate, LocalDateTime endDate){
        this.beginningDate = beginningDate;
        this.endDate = endDate;
    }

    public static PeriodDateTime of(LocalDateTime beginningDate, LocalDateTime endDate) {
        Objects.requireNonNull(beginningDate, "Data początkowa nie może być nullem");
        if(endDate != null && (beginningDate.isEqual(endDate) || beginningDate.isAfter(endDate))){
            throw new IllegalArgumentException("Data początkowa nie może być mniejsza, bądź równa dacie końcowej");
        }
        return new PeriodDateTime(beginningDate, endDate);
    }

    public boolean isBetween(LocalDateTime date){
        if(date == null){
            return endDate == null;
        }
        return date.isEqual(beginningDate) || (date.isAfter(beginningDate) && (endDate == null || date.isEqual(endDate) || date.isBefore(endDate)));
    }

    public boolean isOverlap(PeriodDateTime period){
        return !((period.beginningDate.isBefore(beginningDate) && period.endDate != null && period.endDate.isBefore(beginningDate)) ||
                (endDate != null && period.beginningDate.isAfter(endDate))
        );
    }

    public LocalDateTime getBeginningDate() {
        return beginningDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public PeriodTime toTime() {
        return PeriodTime.of(beginningDate.toLocalTime(), endDate.toLocalTime());
    }

}
