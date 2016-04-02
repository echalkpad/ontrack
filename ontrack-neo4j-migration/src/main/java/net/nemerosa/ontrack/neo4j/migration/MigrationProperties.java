package net.nemerosa.ontrack.neo4j.migration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties
@Component
@Data
public class MigrationProperties {

    private Neo4JProperties neo4j = new Neo4JProperties();

    @Data
    public static class Neo4JProperties {

        private String url = "http://localhost:7474";
        private String username;
        private String password;

    }

}
