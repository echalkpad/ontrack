package net.nemerosa.ontrack.boot.resources;

import net.nemerosa.ontrack.boot.ui.*;
import net.nemerosa.ontrack.model.security.ValidationStampDelete;
import net.nemerosa.ontrack.model.security.ValidationStampEdit;
import net.nemerosa.ontrack.model.structure.ProjectEntityType;
import net.nemerosa.ontrack.model.structure.ValidationStamp;
import net.nemerosa.ontrack.ui.resource.AbstractResourceDecorator;
import net.nemerosa.ontrack.ui.resource.Link;
import net.nemerosa.ontrack.ui.resource.ResourceContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@Component
public class ValidationStampResourceDecorator extends AbstractResourceDecorator<ValidationStamp> {

    public ValidationStampResourceDecorator() {
        super(ValidationStamp.class);
    }

    @Override
    public List<Link> links(ValidationStamp validationStamp, ResourceContext resourceContext) {
        return resourceContext.links()
                .self(on(ValidationStampController.class).getValidationStamp(validationStamp.getId()))
                // Branch link
                .link("_branch", on(BranchController.class).getBranch(validationStamp.getBranch().getId()))
                // Project link
                .link("_project", on(ProjectController.class).getProject(validationStamp.getBranch().getProject().getId()))
                // Image link
                .link(Link.IMAGE_LINK, on(ValidationStampController.class).getValidationStampImage_(null, validationStamp.getId()))
                // Update link
                .update(on(ValidationStampController.class).updateValidationStampForm(validationStamp.getId()), ValidationStampEdit.class, validationStamp.projectId())
                // Delete link
                .delete(on(ValidationStampController.class).deleteValidationStamp(validationStamp.getId()), ValidationStampDelete.class, validationStamp.projectId())
                // TODO Next validation stamp
                // TODO Previous validation stamp
                // Actual properties for this validation stamp
                .link("_properties", on(PropertyController.class).getProperties(ProjectEntityType.VALIDATION_STAMP, validationStamp.getId()))
                // Decorations
                .link("_decorations", on(DecorationsController.class).getDecorations(validationStamp.getProjectEntityType(), validationStamp.getId()))
                // List of runs
                .link("_runs", on(ValidationRunController.class).getValidationRunsForValidationStamp(validationStamp.getId(), 0, 10))
                // Events
                .link("_events", on(EventController.class).getEvents(validationStamp.getProjectEntityType(), validationStamp.getId(), 0, 10))
                // Page
                .page(validationStamp)
                // OK
                .build();
    }

}
