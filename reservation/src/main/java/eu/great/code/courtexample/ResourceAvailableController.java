package eu.great.code.courtexample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.great.code.courtexample.reservation.PeriodDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
@RequestMapping("/")
class ResourceAvailableController {

    private final ResourceRepository resourceRepository;
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    ResourceAvailableController(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @GetMapping("{context}/{resourceUuid}")
    ResponseEntity<Void> isAvailable(
            @PathVariable("context") String context,
            @PathVariable("resourceUuid") UUID resourceUuid){
        Resource resource = resourceRepository.findResourceByResourceUUIDAndContext(resourceUuid, context)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono zasobu"));
        return resource.isAvailable() ?
                ResponseEntity.ok().build() :
                ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
    }

    @GetMapping("{context}/{resourceUuid}/time")
    ResponseEntity<Void> isAvailableInPeriod(
            @PathVariable("context") String context,
            @PathVariable("resourceUuid") UUID resourceUuid,
            @RequestParam String periodDateTime) throws JsonProcessingException {
        PeriodDateTime period = objectMapper.readValue(periodDateTime, PeriodDateTime.class);
        Resource resource = resourceRepository.findResourceByResourceUUIDAndContext(resourceUuid, context)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono zasobu"));
        return resource.isAvailable(period) ?
                ResponseEntity.ok().build() :
                ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
    }

}
