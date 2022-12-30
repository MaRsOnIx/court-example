package eu.great.code.courtexample.court.judge;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "reservation", url = "http://localhost:7075")
interface ResourceAvailabilityClient {
    @GetMapping("{context}/{resourceUuid}")
    void checkAvailability(@PathVariable("resourceUuid") UUID resourceUuid, @PathVariable("context") String context);
}
