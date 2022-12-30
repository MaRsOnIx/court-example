package eu.great.code.courtexample;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
@RequestMapping("/")
class ResourceAvailableController {

    private final ResourceRepository resourceRepository;

    ResourceAvailableController(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @GetMapping("{context}/{resourceUuid}")
    ResponseEntity<Void> isAvailable(@PathVariable("context") String context, @PathVariable("resourceUuid") UUID resourceUuid){
        Resource resource = resourceRepository.findResourceByResourceUUIDAndContext(resourceUuid, context)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono zasobu"));
        return resource.isAvailable() ?
                ResponseEntity.ok().build() :
                ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
    }

}
