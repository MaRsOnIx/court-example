package eu.great.code.courtexample;

import eu.great.code.courtexample.reservation.command.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
class ResourceCommandHandler {

    private final ResourceRepository resourceRepository;

    ResourceCommandHandler(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public Result handle(BecomeAvailableAsDefaultCommand command){
        Resource resource = resourceRepository.findResourceByResourceUUIDAndContext(command.resourceUuid(), command.context())
                .orElse(new Resource(command.resourceUuid(), command.context()));
        resource.available();
        resourceRepository.save(resource);
        return Result.success();
    }

    @Transactional
    public Result handle(BecomeUnavailableAsDefaultCommand command){
        Resource resource = resourceRepository.findResourceByResourceUUIDAndContext(command.resourceUuid(), command.context())
                .orElse(new Resource(command.resourceUuid(), command.context()));
        resource.unavailable();
        resourceRepository.save(resource);
        return Result.success();
    }

    @Transactional
    public Result handle(RemoveEachReservationCommand command){
        Optional<Resource> optionalResource = resourceRepository.findResourceByResourceUUIDAndContext(command.resourceUuid(), command.context());
        if(optionalResource.isEmpty()){
            return Result.failure("Nie znaleziono zasobu");
        }
        Resource resource = optionalResource.get();
        resource.removeFeatureReservations();
        return Result.success();
    }

    @Transactional
    public Result handle(PeriodicallyReserveAvailabilityCommand command){
        Optional<Resource> optionalResource = resourceRepository.findResourceByResourceUUIDAndContext(
                command.resourceUuid(), command.context());
        if(optionalResource.isEmpty()){
            return Result.failure("Nie znaleziono zasobu");
        }
        Resource resource = optionalResource.get();
        resource.reserveAvailable(command.instant(), command.startDate(), command.endDate());
        return Result.success();
    }

    @Transactional
    public Result handle(PeriodicallyReserveUnavailabilityCommand command){
        Optional<Resource> optionalResource = resourceRepository.findResourceByResourceUUIDAndContext(command.resourceUuid(), command.context());
        if(optionalResource.isEmpty()){
            return Result.failure("Nie znaleziono zasobu");
        }
        Resource resource = optionalResource.get();
        resource.reserveUnavailable(command.instant(), command.startDate(), command.endDate());
        return Result.success();
    }

}

sealed interface Result {
    static Result failure(String message){
        return new Failure(message);
    }
    static Result success(){
        return new Success();
    }
    final class Success implements Result{
        private Success() {}
    }
    final class Failure implements Result{
        private final String message;

        private Failure(String message) {
            this.message = message;
        }

        String getMessage() {
            return message;
        }
    }
}


