package net.nemerosa.ontrack.model.support;

import lombok.Data;
import net.nemerosa.ontrack.model.structure.Describable;

@Data
public class NameValue {

    private final String name;
    private final String value;

    public static NameValue of(Describable describable) {
        return new NameValue(describable.getId(), describable.getName());
    }

}
