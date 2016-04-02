package net.nemerosa.ontrack.neo4j.migration;

import com.google.common.collect.ImmutableMap;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.repository.StructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.stereotype.Component;

@Component
public class Migration {

    private final Logger logger = LoggerFactory.getLogger(Migration.class);

    private final StructureRepository structure;
    private final Neo4jTemplate template;

    @Autowired
    public Migration(StructureRepository structure, Neo4jTemplate template) {
        this.structure = structure;
        this.template = template;
    }

    public void run() {
        logger.info("Starting migration...");
        long start = System.currentTimeMillis();
        migrateProjects();
        long end = System.currentTimeMillis();
        logger.info("End of migration ({} ms)", end - start);
    }

    private void migrateProjects() {
        // Gets the list of projects
        structure.getProjectList().forEach(this::migrateProject);
    }

    private void migrateProject(Project project) {
        logger.info("Migrating project {}...", project.getName());
        template.query(
                "MERGE (p:Project {name: {name}, description: {description}})",
                ImmutableMap.<String, Object>builder()
                        .put("name", project.getName())
                        .put("description", safeString(project.getDescription()))
                        .build()
        );
    }

    private String safeString(String description) {
        return description == null ? "" : description;
    }

}
