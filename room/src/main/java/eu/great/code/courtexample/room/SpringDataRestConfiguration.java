package eu.great.code.courtexample.room;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
class SpringDataRestConfiguration implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.getExposureConfiguration()
                .forDomainType(Room.class)
                .withItemExposure((metdata, httpMethods) -> httpMethods.disable(HttpMethod.DELETE))
                .withCollectionExposure((metdata, httpMethods) -> httpMethods.disable(HttpMethod.DELETE));
    }
}
