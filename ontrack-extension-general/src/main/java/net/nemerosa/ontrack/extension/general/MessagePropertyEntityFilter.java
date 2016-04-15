package net.nemerosa.ontrack.extension.general;

import net.nemerosa.ontrack.model.structure.EntityFilter;
import net.nemerosa.ontrack.model.structure.ProjectEntity;
import net.nemerosa.ontrack.model.structure.ProjectEntityType;
import net.nemerosa.ontrack.model.structure.PropertyService;
import net.nemerosa.ontrack.model.support.NameValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        return "Messages";
    }

    @Override
    public List<NameValue> getOptions() {
        return Arrays.asList(MessageType.values()).stream()
                .map(NameValue::of)
                .collect(Collectors.toList());
    }

    @Override
    public boolean accept(ProjectEntity entity, String value) {
        return propertyService.getProperty(entity, MessagePropertyType.class).option()
                .map(messageProperty -> StringUtils.equals(value, messageProperty.getType().getId()))
                .orElse(false);
    }
}
