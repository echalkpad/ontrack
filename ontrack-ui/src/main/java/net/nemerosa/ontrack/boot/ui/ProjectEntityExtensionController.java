package net.nemerosa.ontrack.boot.ui;

import net.nemerosa.ontrack.extension.api.EntityInformationExtension;
import net.nemerosa.ontrack.extension.api.ExtensionManager;
import net.nemerosa.ontrack.extension.api.ProjectEntityActionExtension;
import net.nemerosa.ontrack.extension.api.model.EntityInformation;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.model.support.Action;
import net.nemerosa.ontrack.ui.resource.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

/**
 * Controller used to get extensions on project entities.
 */
@RestController
@RequestMapping("/extensions/entity")
public class ProjectEntityExtensionController extends AbstractProjectEntityController {

    private final ExtensionManager extensionManager;
    private final EntityFilterService entityFilterService;

    @Autowired
    public ProjectEntityExtensionController(StructureService structureService, ExtensionManager extensionManager, EntityFilterService entityFilterService) {
        super(structureService);
        this.extensionManager = extensionManager;
        this.entityFilterService = entityFilterService;
    }

    /**
     * Gets the list of actions for an entity
     */
    @RequestMapping(value = "actions/{entityType}/{id}", method = RequestMethod.GET)
    public Resources<Action> getActions(@PathVariable ProjectEntityType entityType, @PathVariable ID id) {
        return Resources.of(
                extensionManager.getExtensions(ProjectEntityActionExtension.class).stream()
                        .map(x -> x.getAction(getEntity(entityType, id)).map(action -> resolveExtensionAction(x, action)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()),
                uri(on(getClass()).getActions(entityType, id))
        );
    }

    /**
     * Gets the list of information extensions for an entity
     */
    @RequestMapping(value = "information/{entityType}/{id}", method = RequestMethod.GET)
    public Resources<EntityInformation> getInformation(@PathVariable ProjectEntityType entityType, @PathVariable ID id) {
        // Gets the entity
        ProjectEntity entity = getEntity(entityType, id);
        // List of informations to return
        List<EntityInformation> informations = extensionManager.getExtensions(EntityInformationExtension.class).stream()
                .map(x -> x.getInformation(entity))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // OK
        return Resources.of(
                informations,
                uri(on(getClass()).getInformation(entityType, id))
        );
    }

    /**
     * Gets the list of filters for an entity
     */
    @RequestMapping(value = "filter/{entityType}", method = RequestMethod.GET)
    public Resources<EntityFilterDescription> getEntityFilters(@PathVariable ProjectEntityType entityType) {
        return Resources.of(
                entityFilterService.getEntityFilters().stream()
                        .filter(f -> f.getScope().contains(entityType))
                        .map(EntityFilter::getEntityFilterDescription)
                        .collect(Collectors.toList()),
                uri(on(getClass()).getEntityFilters(entityType))
        );
    }

}
