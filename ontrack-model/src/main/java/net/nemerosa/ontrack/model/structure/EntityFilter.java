package net.nemerosa.ontrack.model.structure;

import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.model.form.Field;

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
     * Configuration filed
     */
    Field getField();

    /**
     * Filter on an entity.
     *
     * @param entity Entity to filter
     * @param value  The JSON is the result of the evaluation of the {@linkplain #getField() form input}.
     */
    boolean accept(ProjectEntity entity, JsonNode value);

    /**
     * Gets the description for the filter, to be sent to a client.
     */
    default EntityFilterDescription getEntityFilterDescription() {
        return new EntityFilterDescription(
                getClass().getName(),
                getTitle(),
                getField()
        );
    }

}
