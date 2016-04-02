package net.nemerosa.ontrack.neo4j.migration;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.data.neo4j.server.RemoteServer;

@SpringBootApplication
public class MigrationTool extends Neo4jConfiguration {

    @Autowired
    private MigrationProperties migrationProperties;

    @Autowired
    private Migration migration;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MigrationTool.class);
        ConfigurableApplicationContext context = application.run(args);
        context.getBeansOfType(Migration.class).get("migration").run();
    }

    @Override
    public Neo4jServer neo4jServer() {
        return new RemoteServer(
                migrationProperties.getNeo4j().getUrl(),
                migrationProperties.getNeo4j().getUsername(),
                migrationProperties.getNeo4j().getPassword()
        );
    }

    @Override
    public SessionFactory getSessionFactory() {
        return new SessionFactory("net.nemerosa.ontrack.neo4j.domain");
    }

}
