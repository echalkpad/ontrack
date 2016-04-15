package net.nemerosa.ontrack.service;

import net.nemerosa.ontrack.model.structure.EntityFilter;
import net.nemerosa.ontrack.model.structure.EntityFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntityFilterServiceImpl implements EntityFilterService {

    private final List<EntityFilter> entityFilters;

    @Autowired
    public EntityFilterServiceImpl(List<EntityFilter> entityFilters) {
        this.entityFilters = entityFilters;
    }

    @Override
    public List<EntityFilter> getEntityFilters() {
        return entityFilters;
    }
}
