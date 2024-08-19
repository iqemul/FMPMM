package edu.hit.fmpmm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableRedisRepositories
public class FmpmmApplication {

    public static void main(String[] args) {
        // https://spring.io/projects/spring-data-neo4j#learn
        // https://docs.spring.io/spring-data/neo4j/reference/object-mapping/metadata-based-mapping.html
        // https://manual.coppeliarobotics.com/en/zmqRemoteApiOverview.htm#java
        SpringApplication.run(FmpmmApplication.class, args);
    }

}
