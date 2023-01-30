package eu.great.code.courtexample.term;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
class TermApplication {

    public static void main(String[] args) {
        SpringApplication.run(TermApplication.class, args);
    }

}



