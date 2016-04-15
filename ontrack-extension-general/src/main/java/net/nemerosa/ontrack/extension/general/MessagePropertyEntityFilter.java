package net.nemerosa.ontrack.extension.general;

import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.json.JsonUtils;
import net.nemerosa.ontrack.model.form.Field;
import net.nemerosa.ontrack.model.form.Selection;
import net.nemerosa.ontrack.model.structure.EntityFilter;
import net.nemerosa.ontrack.model.structure.ProjectEntity;
import net.nemerosa.ontrack.model.structure.ProjectEntityType;
import net.nemerosa.ontrack.model.structure.PropertyService;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class MessagePropertyEntityFilter implements EntityFilter {

    private final PropertyService propertyService;

    @Autowired
    public MessagePropertyEntityFilter(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Override
    public Set<ProjectEntityType> getScope() {
        return EnumSet.allOf(ProjectEntityType.class);
    }

    @Override
    public String getTitle() {
        return "Filter on message type";
    }

    @Override
    public Field getField() {
        return Selection.of("type").label("Type")
                .optional()
                .items(EnumUtils.getEnumList(MessageType.class));
    }

    @Override
    public boolean accept(ProjectEntity entity, JsonNode value) {
        String type = JsonUtils.get(value, "type", false, null);
        if (StringUtils.isNotBlank(type)) {
            return propertyService.getProperty(entity, MessagePropertyType.class).option()
                    .map(messageProperty -> StringUtils.equals(type, messageProperty.getType().getId()))
                    .orElse(false);
        } else {
            return true;
        }
    }
}
