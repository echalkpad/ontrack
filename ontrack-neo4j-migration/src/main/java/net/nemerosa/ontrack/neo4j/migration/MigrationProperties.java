package net.nemerosa.ontrack.neo4j.migration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ontrack.migration")
@Component
@Data
public class MigrationProperties {

}
