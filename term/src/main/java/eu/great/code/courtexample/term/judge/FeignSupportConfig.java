package eu.great.code.courtexample.term.judge;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FeignSupportConfig {

    @Bean
    ErrorDecoder errorDecoder() {
        return new RetreiveMessageErrorDecoder();
    }
}
