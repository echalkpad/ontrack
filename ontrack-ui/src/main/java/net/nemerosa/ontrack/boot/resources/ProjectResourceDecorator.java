package net.nemerosa.ontrack.boot.resources;

import net.nemerosa.ontrack.boot.ui.*;
import net.nemerosa.ontrack.model.security.*;
import net.nemerosa.ontrack.model.structure.Project;
import net.nemerosa.ontrack.model.structure.ProjectEntityType;
import net.nemerosa.ontrack.ui.resource.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@Component
public class ProjectResourceDecorator extends AbstractResourceDecorator<Project> {

    private final ResourceDecorationContributorService resourceDecorationContributorService;

    @Autowired
    public ProjectResourceDecorator(ResourceDecorationContributorService resourceDecorationContributorService) {
        super(Project.class);
        this.resourceDecorationContributorService = resourceDecorationContributorService;
    }

    @Override
    public List<Link> links(Project project, ResourceContext resourceContext) {
        LinksBuilder linksBuilder = resourceContext.links()
                .self(on(ProjectController.class).getProject(project.getId()))
                // List of branches for this project
                .link("_branches", on(BranchController.class).getBranchListForProject(project.getId()))
                // Creates a branch for this project
                .link(
                        "_createBranch",
                        on(BranchController.class).newBranchForm(project.getId()),
                        BranchCreate.class, project
                )
                // List of branches and their views
                .link("_branchStatusViews", on(ProjectController.class).getBranchStatusViews(project.getId()))
                // Build search
                .link("_buildSearch", on(BuildController.class).buildSearchForm(project.getId()))
                // Build diff actions
                .link("_buildDiffActions", on(BuildController.class).buildDiffActions(project.getId()))
                // Actual properties for this project
                .link("_properties", on(PropertyController.class).getProperties(ProjectEntityType.PROJECT, project.getId()))
                // Actions
                .link("_actions", on(ProjectEntityExtensionController.class).getActions(ProjectEntityType.PROJECT, project.getId()))
                // Updating the project
                .update(on(ProjectController.class).saveProject(project.getId(), null), ProjectEdit.class, project.id())
                // Delete link
                .delete(on(ProjectController.class).deleteProject(project.getId()), ProjectDelete.class, project.id())
                // Decorations
                .link("_decorations", on(DecorationsController.class).getDecorations(project.getProjectEntityType(), project.getId()))
                // Authorisation management
                .link("_permissions", on(PermissionController.class).getProjectPermissions(project.getId()), ProjectAuthorisationMgt.class, project.id())
                // Events
                .link("_events", on(EventController.class).getEvents(project.getProjectEntityType(), project.getId(), 0, 10))
                // Clone to another project
                .link(
                        "_clone",
                        on(ProjectController.class).clone(project.getId()),
                        ProjectCreation.class
                )
                // Enable
                .link(
                        "_enable",
                        on(ProjectController.class).enableProject(project.getId()),
                        resourceContext.isProjectFunctionGranted(project.id(), ProjectEdit.class)
                                && project.isDisabled()
                )
                // Disable
                .link(
                        "_disable",
                        on(ProjectController.class).disableProject(project.getId()),
                        resourceContext.isProjectFunctionGranted(project.id(), ProjectEdit.class)
                                && !project.isDisabled()
                )
                // Page
                .page(project);
        // Contributions
        resourceDecorationContributorService.contribute(linksBuilder, project);
        // OK
        return linksBuilder.build();
    }
}
