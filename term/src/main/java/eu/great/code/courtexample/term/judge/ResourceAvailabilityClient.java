package eu.great.code.courtexample.term.judge;

import eu.great.code.courtexample.reservation.PeriodDateTime;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "reservation", url = "http://localhost:7075")
interface ResourceAvailabilityClient {
    @GetMapping("{context}/{resourceUuid}")
    void checkAvailability(@PathVariable("resourceUuid") UUID resourceUuid, @PathVariable("context") String context);
    @GetMapping("{context}/{resourceUuid}/time")
    void checkAvailabilityInTime(@PathVariable("resourceUuid") UUID resourceUuid, @PathVariable("context") String context, @RequestParam String periodDateTime);
}
