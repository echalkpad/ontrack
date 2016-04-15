package net.nemerosa.ontrack.model.structure;

import net.nemerosa.ontrack.model.support.NameValue;

import java.util.List;
import java.util.Set;

/**
 * Filter on entities.
 */
public interface EntityFilter {

    /**
     * Scope
     */
    Set<ProjectEntityType> getScope();

    /**
     * Title of the filter
     */
    String getTitle();

    /**
     * List of options, as name/values
     */
    List<NameValue> getOptions();

    /**
     * Filter on an entity
     */
    boolean accept(ProjectEntity entity, String value);

}
