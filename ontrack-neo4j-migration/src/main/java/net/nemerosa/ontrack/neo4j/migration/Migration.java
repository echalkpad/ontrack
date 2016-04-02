package net.nemerosa.ontrack.neo4j.migration;

import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.repository.StructureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Migration {

    private final Logger logger = LoggerFactory.getLogger(Migration.class);

    private final StructureRepository structure;

    @Autowired
    public Migration(StructureRepository structure) {
        this.structure = structure;
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
    }

}
