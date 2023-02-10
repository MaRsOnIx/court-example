package eu.great.code.courtexample;

import eu.great.code.courtexample.reservation.command.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
class ReservationCommandListener {

    private final ResourceCommandHandler handler;
    private final Logger logger = LogManager.getLogger(ReservationCommandListener.class);

    ReservationCommandListener(ResourceCommandHandler handler) {
        this.handler = handler;
    }

    @KafkaListener(topics = "reservation")
    void onCommand(ReservationCommand command){
        logger.info("Handling command {}", command.getClass().getSimpleName());
        switch (command){
            case BecomeAvailableAsDefaultCommand c -> logExecutor(() -> handler.handle(c));
            case BecomeUnavailableAsDefaultCommand c -> logExecutor(() -> handler.handle(c));
            case PeriodicallyReserveAvailabilityCommand c -> logExecutor(() -> handler.handle(c));
            case PeriodicallyReserveUnavailabilityCommand c -> logExecutor(() -> handler.handle(c));
            case RemoveEachReservationCommand c -> logExecutor(() -> handler.handle(c));
        }
    }

    private void logExecutor(Supplier<Result> func){
        Result result = func.get();
        if(result instanceof Result.Failure failure){
            logger.error(failure.getMessage());
        }
    }

}
