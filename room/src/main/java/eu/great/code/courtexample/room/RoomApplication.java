package eu.great.code.courtexample.room;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
class RoomApplication {

    public static void main(String[] args) {
        if(true){
            List<LocalDate> collect = List.of(LocalDate.of(2000, 1, 1), LocalDate.of(2020, 1, 1))
                    .stream()
                    .sorted(LocalDate::compareTo)
                    .collect(Collectors.toList());
            System.out.println(collect.get(0));
            System.out.println();
        }

        SpringApplication.run(RoomApplication.class, args);
    }

}



