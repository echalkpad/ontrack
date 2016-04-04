package net.nemerosa.ontrack.neo4j.migration;

import com.google.common.collect.ImmutableMap;
import net.nemerosa.ontrack.common.Time;
import net.nemerosa.ontrack.model.events.EventFactory;
import net.nemerosa.ontrack.model.events.EventType;
import net.nemerosa.ontrack.model.structure.Branch;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.PromotionLevel;
import net.nemerosa.ontrack.model.structure.Signature;
import net.nemerosa.ontrack.repository.StructureRepository;
import org.neo4j.ogm.session.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class Migration extends NamedParameterJdbcDaoSupport {

    private final Logger logger = LoggerFactory.getLogger(Migration.class);

    private final StructureRepository structure;
    private final Neo4jTemplate template;

    @Autowired
    public Migration(StructureRepository structure, Neo4jTemplate template, DataSource dataSource) {
        this.structure = structure;
        this.template = template;
        this.setDataSource(dataSource);
    }

    public void run() {
        logger.info("Starting migration...");
        long start = System.currentTimeMillis();
        // Deleting all nodes
        logger.info("Removing all nodes...");
        template.query("MATCH (n) DETACH DELETE n", Collections.emptyMap());
        // Migrating the projects
        logger.info("Migrating projects...");
        migrateProjects();
        // Creating the counters
        logger.info("Creating unique id generators...");
        createUniqueIdGenerators();
        // OK
        long end = System.currentTimeMillis();
        logger.info("End of migration ({} ms)", end - start);
    }

    private void createUniqueIdGenerators() {
        createUniqueIdGenerator("Project");
        createUniqueIdGenerator("Branch");
        createUniqueIdGenerator("PromotionLevel");
    }

    private void createUniqueIdGenerator(String label) {
        Result query = template.query(String.format("MATCH (p:%s) RETURN MAX(p.id) as MAX", label),
                ImmutableMap.<String, Object>builder()
                        .put("label", label)
                        .build());
        int max = (Integer) query.queryResults().iterator().next().get("MAX");
        template.query(
                "CREATE (u:UniqueId {label: {label}, id: {id}})",
                ImmutableMap.<String, Object>builder()
                        .put("label", label)
                        .put("id", max)
                        .build()
        );
    }

    private void migrateProjects() {
        // Gets the list of projects
        structure.getProjectList().forEach(this::migrateProject);
    }

    private void migrateProject(Project project) {
        logger.info("Migrating project {}...", project.getName());

        Signature signature = getEventSignature("project", EventFactory.NEW_PROJECT, project.id());
        template.query(
                "CREATE (p:Project {id: {id}, name: {name}, description: {description}, createdAt: {createdAt}, createdBy: {createdBy}})",
                ImmutableMap.<String, Object>builder()
                        .put("id", project.id())
                        .put("name", project.getName())
                        .put("description", safeString(project.getDescription()))
                        .put("createdAt", Time.toJavaUtilDate(signature.getTime()))
                        .put("createdBy", signature.getUser().getName())
                        .build()
        );

        structure.getBranchesForProject(project.getId()).forEach(this::migrateBranch);
    }

    private void migrateBranch(Branch branch) {
        logger.info("Migrating branch {}:{}...", branch.getProject().getName(), branch.getName());
        Signature signature = getEventSignature("branch", EventFactory.NEW_BRANCH, branch.id());
        template.query(
                "MATCH (p:Project {id: {projectId}}) " +
                        "CREATE (b:Branch {id: {id}, name: {name}, description: {description}, createdAt: {createdAt}, createdBy: {createdBy}, disabled: {disabled}})" +
                        "-[:BRANCH_OF]->(p)",
                ImmutableMap.<String, Object>builder()
                        .put("id", branch.id())
                        .put("projectId", branch.getProject().id())
                        .put("name", branch.getName())
                        .put("description", safeString(branch.getDescription()))
                        .put("createdAt", Time.toJavaUtilDate(signature.getTime()))
                        .put("createdBy", signature.getUser().getName())
                        .put("disabled", branch.isDisabled())
                        .build()
        );
        List<PromotionLevel> promotionLevels = structure.getPromotionLevelListForBranch(branch.getId());
        int orderNb = 0;
        for (PromotionLevel promotionLevel : promotionLevels) {
            migratePromotionLevel(promotionLevel, ++orderNb);
        }
    }

    private void migratePromotionLevel(PromotionLevel promotionLevel, int orderNb) {
        logger.info("Migrating promotion level {}:{}:{}...", promotionLevel.getProject().getName(), promotionLevel.getBranch().getName(), promotionLevel.getName());
        Signature signature = getEventSignature("promotion_level", EventFactory.NEW_PROMOTION_LEVEL, promotionLevel.id());
        template.query(
                "MATCH (b:Branch {id: {branchId}}) " +
                        "CREATE (pl:PromotionLevel {id: {id}, name: {name}, description: {description}, createdAt: {createdAt}, createdBy: {createdBy}, orderNb: {orderNb}})" +
                        "-[:PROMOTION_LEVEL_OF]->(b)",
                ImmutableMap.<String, Object>builder()
                        .put("id", promotionLevel.id())
                        .put("branchId", promotionLevel.getBranch().id())
                        .put("name", promotionLevel.getName())
                        .put("description", safeString(promotionLevel.getDescription()))
                        .put("createdAt", Time.toJavaUtilDate(signature.getTime()))
                        .put("createdBy", signature.getUser().getName())
                        .put("orderNb", orderNb)
                        // TODO Image type
                        // TODO Image bytes - in a separate file
                        .build()
        );
    }

    private Signature getEventSignature(String entity, EventType eventType, int entityId) {
        String eventUser;
        LocalDateTime eventTime;
        List<Map<String, Object>> results = getNamedParameterJdbcTemplate().queryForList(
                String.format(
                        "SELECT * FROM EVENTS WHERE %s = :entityId AND EVENT_TYPE = :type ORDER BY ID DESC LIMIT 1",
                        entity
                ),
                ImmutableMap.<String, Object>builder()
                        .put("entityId", entityId)
                        .put("type", eventType.getId())
                        .build()
        );
        if (results.isEmpty()) {
            eventUser = "import";
            eventTime = Time.now();
        } else {
            Map<String, Object> result = results.get(0);
            eventUser = (String) result.get("event_user");
            eventTime = Time.fromStorage((String) result.get("event_time"));
        }
        return Signature.of(eventTime, eventUser);
    }

    private String safeString(String description) {
        return description == null ? "" : description;
    }

}
