package net.nemerosa.ontrack.extension.general;

import com.fasterxml.jackson.databind.JsonNode;
import net.nemerosa.ontrack.json.JsonUtils;
import net.nemerosa.ontrack.model.form.Field;
import net.nemerosa.ontrack.model.form.Text;
import net.nemerosa.ontrack.model.structure.EntityFilter;
import net.nemerosa.ontrack.model.structure.ProjectEntity;
import net.nemerosa.ontrack.model.structure.ProjectEntityType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class ProjectNameEntityFilter implements EntityFilter {

    @Override
    public Set<ProjectEntityType> getScope() {
        return EnumSet.of(ProjectEntityType.PROJECT);
    }

    @Override
    public String getTitle() {
        return "Filter on project name";
    }

    @Override
    public Field getField() {
        return Text.of("name").label("Name").optional();
    }

    @Override
    public boolean accept(ProjectEntity entity, JsonNode value) {
        String name = JsonUtils.get(value, "name", false, null);
        return StringUtils.isBlank(name) || StringUtils.containsIgnoreCase(entity.getProject().getName(), name);
    }
}
