package net.nemerosa.ontrack.model.structure;

import lombok.Data;
import net.nemerosa.ontrack.model.form.Field;

/**
 * Filter on entities.
 */
@Data
public class EntityFilterDescription {

    /**
     * Type
     */
    private final String type;

    /**
     * Title of the filter
     */
    private final String title;

    /**
     * Configuration field
     */
    private final Field field;

}
