package net.nemerosa.ontrack.neo4j.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Migration {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Migration.class);
        application.run(args);
    }
}
