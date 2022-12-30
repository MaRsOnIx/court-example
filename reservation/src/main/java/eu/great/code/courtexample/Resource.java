package eu.great.code.courtexample;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Entity
class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;
    private UUID resourceUUID;
    private String context;
    private Status defaultStatus = Status.AVAILABLE;
    @OneToMany(fetch = FetchType.EAGER)
    private final Set<Period> periods = new HashSet<>();


    protected Resource() {}

    Resource(UUID resourceUUID, String context) {
        this.resourceUUID = resourceUUID;
        this.context = context;
    }

    void available(){
        this.defaultStatus = Status.AVAILABLE;
    }

    void unavailable(){
        this.defaultStatus = Status.UNAVAILABLE;
    }

    void reserveAvailable(Instant causeOccurred, LocalDateTime startDate, LocalDateTime endDate){
        periods.add(new Period(causeOccurred, Status.AVAILABLE, startDate, endDate));
    }

    void reserveUnavailable(Instant causeOccurred, LocalDateTime startDate, LocalDateTime endDate){
        periods.add(new Period(causeOccurred, Status.UNAVAILABLE, startDate, endDate));
    }

    void removeFeatureReservations(){
        LocalDateTime now = LocalDateTime.now();
        periods.removeIf(period -> period.getEndDate().isAfter(now));
    }

    boolean isAvailable(){
        LocalDateTime now = LocalDateTime.now();
        if(periods.isEmpty()){
            return defaultStatus == Status.AVAILABLE;
        }
        Status status = periods.stream()
                .filter(v -> v.isIncluding(now))
                .max(Comparator.comparing(Period::getCauseOccurred))
                .map(Period::getStatus)
                .orElse(defaultStatus);

        return status == Status.AVAILABLE;
    }


    private enum Status {
        AVAILABLE, UNAVAILABLE
    }

    @Entity
    private static class Period {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID periodUUID;
        private Instant causeOccurred;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Status status;

        private Period(Instant causeOccurred, Status status, LocalDateTime startDate, LocalDateTime endDate) {
            this.status = status;
            this.causeOccurred = causeOccurred;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        protected Period() {}

        boolean isIncluding(LocalDateTime date) {
            return (date.isEqual(startDate) || date.isAfter(startDate)) && (date.isEqual(endDate) || date.isBefore(endDate));
        }

        Instant getCauseOccurred() {
            return causeOccurred;
        }

        LocalDateTime getStartDate() {
            return startDate;
        }

        LocalDateTime getEndDate() {
            return endDate;
        }

        Status getStatus() {
            return status;
        }
    }


}
