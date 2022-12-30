package eu.great.code.courtexample.court;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients
class CourtApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(CourtApplication.class, args);
    }

    private final CourtRepository courtRepository;

    CourtApplication(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    @Override
    public void run(String... args) {
        courtRepository.save(new CourtWithDepartmentInstance("Sąd Rejonowy w Warszawie", "IV Wydział Cywilny"));
        courtRepository.save(new CourtWithDepartmentInstance("Sąd Rejonowy w Malborku", "I Wydział Karny"));
    }
}
