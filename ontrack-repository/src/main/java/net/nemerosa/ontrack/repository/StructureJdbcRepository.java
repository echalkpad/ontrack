package net.nemerosa.ontrack.repository;

import net.nemerosa.ontrack.model.exceptions.ProjectNameAlreadyDefinedException;
import net.nemerosa.ontrack.model.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notNull;

@Repository
public class StructureJdbcRepository extends AbstractJdbcRepository implements StructureRepository {

    @Autowired
    public StructureJdbcRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Project newProject(Project project) {
        // Validation
        notNull(project, "Project must be defined");
        isTrue(project.getId() == null || !project.getId().isSet(), "Project ID must not be defined");
        // Creation
        try {
            int id = dbCreate(
                    "INSERT INTO PROJECTS(NAME, DESCRIPTION) VALUES (:name, :description)",
                    params("name", project.getName()).addValue("description", project.getDescription())
            );
            // Returns with ID
            return project.withId(id(id));
        } catch (DuplicateKeyException ex) {
            throw new ProjectNameAlreadyDefinedException(project.getName());
        }
    }

    @Override
    public List<Project> getProjectList() {
        return getJdbcTemplate().query(
                "SELECT * FROM PROJECTS ORDER BY NAME",
                (rs, rowNum) -> toProject(rs)
        );
    }

    @Override
    public Project getProject(ID projectId) {
        return getNamedParameterJdbcTemplate().queryForObject(
                "SELECT * FROM PROJECTS WHERE ID = :id",
                params("id", projectId.getValue()),
                (rs, rowNum) -> toProject(rs)
        );
    }

    @Override
    public void saveProject(Project project) {
        notNull(project, "Project must be defined");
        isTrue(project.getId() != null && project.getId().isSet(), "Project ID must be defined");
        getNamedParameterJdbcTemplate().update(
                "UPDATE PROJECTS SET NAME = :name, DESCRIPTION = :description WHERE ID = :id",
                params("name", project.getName())
                        .addValue("description", project.getDescription())
                        .addValue("id", project.getId().getValue())
        );
    }

    @Override
    public Branch getBranch(ID branchId) {
        return getNamedParameterJdbcTemplate().queryForObject(
                "SELECT * FROM BRANCHES WHERE ID = :id",
                params("id", branchId.getValue()),
                (rs, rowNum) -> toBranch(rs, this::getProject)
        );
    }

    @Override
    public List<Branch> getBranchesForProject(ID projectId) {
        Project project = getProject(projectId);
        return getNamedParameterJdbcTemplate().query(
                "SELECT * FROM BRANCHES WHERE PROJECTID = :projectId ORDER BY NAME",
                params("projectId", projectId.getValue()),
                (rs, rowNum) -> toBranch(rs, id -> project)
        );
    }

    protected Branch toBranch(ResultSet rs, Function<ID, Project> projectSupplier) throws SQLException {
        return Branch.of(
                projectSupplier.apply(id(rs, "projectId")),
                new NameDescription(
                        rs.getString("name"),
                        rs.getString("description")
                )
        ).withId(id(rs));
    }

    protected Project toProject(ResultSet rs) throws SQLException {
        return Project.of(new NameDescription(
                rs.getString("name"),
                rs.getString("description")
        )).withId(id(rs.getInt("id")));
    }

}
