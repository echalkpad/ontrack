package net.nemerosa.ontrack.service;

import net.nemerosa.ontrack.common.Time;
import net.nemerosa.ontrack.model.events.Event;
import net.nemerosa.ontrack.model.events.EventFactory;
import net.nemerosa.ontrack.model.events.EventQueryService;
import net.nemerosa.ontrack.model.job.*;
import net.nemerosa.ontrack.model.settings.CachedSettingsService;
import net.nemerosa.ontrack.model.settings.GeneralSettings;
import net.nemerosa.ontrack.model.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Detection and management of stale branches.
 */
@Component
@Deprecated
public class StaleBranchesJob implements JobProvider {

    private final StructureService structureService;
    private final PropertyService propertyService;
    private final EventQueryService eventQueryService;
    private final CachedSettingsService cachedSettingsService;

    @Autowired
    public StaleBranchesJob(StructureService structureService, PropertyService propertyService, EventQueryService eventQueryService, CachedSettingsService cachedSettingsService) {
        this.structureService = structureService;
        this.propertyService = propertyService;
        this.eventQueryService = eventQueryService;
        this.cachedSettingsService = cachedSettingsService;
    }

    @Override
    public Collection<Job> getJobs() {
        return Collections.singletonList(
                new Job() {
                    @Override
                    public String getCategory() {
                        return "System";
                    }

                    @Override
                    public String getId() {
                        return "StaleBranches";
                    }

                    @Override
                    public String getDescription() {
                        return "Detection and management of stale branches";
                    }

                    @Override
                    public boolean isDisabled() {
                        return false;
                    }

                    /**
                     * Once a day
                     */
                    @Override
                    public int getInterval() {
                        return Job.DAY;
                    }

                    @Override
                    public JobTask createTask() {
                        return new RunnableJobTask(StaleBranchesJob.this::detectAndManageStaleBranches);
                    }
                }
        );
    }

    protected void detectAndManageStaleBranches(JobInfoListener infoListener) {
        // Disabling and deletion times
        GeneralSettings settings = cachedSettingsService.getCachedSettings(GeneralSettings.class);
        int disablingDuration = settings.getDisablingDuration();
        int deletionDuration = settings.getDeletingDuration();
        // Nothing to do if no disabling time
        if (disablingDuration <= 0) {
            infoListener.post("No disabling time being set - exiting.");
        } else {
            // Current time
            LocalDateTime now = Time.now();
            // Disabling time
            LocalDateTime disablingTime = now.minusDays(disablingDuration);
            // Deletion time
            Optional<LocalDateTime> deletionTime =
                    Optional.ofNullable(
                            deletionDuration > 0 ?
                                    disablingTime.minusDays(deletionDuration) :
                                    null
                    );
            // Logging
            infoListener.post(format("Disabling time: %s", disablingTime));
            infoListener.post(format("Deletion time: %s", deletionTime));
            // For all projects
            structureService.getProjectList().forEach(
                    project -> detectAndManageStaleBranches(infoListener, project, disablingTime, deletionTime)
            );
        }
    }

    protected void detectAndManageStaleBranches(JobInfoListener infoListener, Project project, LocalDateTime disablingTime, Optional<LocalDateTime> deletionTime) {
        // FIXME Deletion/desactivation is activated at project?
        infoListener.post(format("[%s] Scanning project for stale branches", project.getName()));
        structureService.getBranchesForProject(project.getId()).forEach(
                branch -> detectAndManageStaleBranch(infoListener, branch, disablingTime, deletionTime)
        );
    }

    protected void detectAndManageStaleBranch(JobInfoListener infoListener, Branch branch, LocalDateTime disablingTime, Optional<LocalDateTime> deletionTime) {
        infoListener.post(format("[%s][%s] Scanning branch for staleness", branch.getProject().getName(), branch.getName()));
        // Build filter to get the last build
        StandardBuildFilter filter = new StandardBuildFilter(
                StandardBuildFilterData.of(1),
                propertyService
        );
        // Templates are excluded
        if (branch.getType() == BranchType.TEMPLATE_DEFINITION) {
            infoListener.post(format("[%s][%s] Branch templates are not eligible for staleness", branch.getProject().getName(), branch.getName()));
            return;
        }
        // Last date
        LocalDateTime lastTime;
        // Last build on this branch
        List<Build> builds = structureService.getFilteredBuilds(branch.getId(), filter);
        if (builds.isEmpty()) {
            infoListener.post(format("[%s][%s] No available build - taking branch's creation time", branch.getProject().getName(), branch.getName()));
            // Takes the branch creation time
            List<Event> events = eventQueryService.getEvents(
                    ProjectEntityType.BRANCH,
                    branch.getId(),
                    EventFactory.NEW_BRANCH,
                    0,
                    1
            );
            if (events.isEmpty()) {
                infoListener.post(format("[%s][%s] No available branch creation date - keeping the branch", branch.getProject().getName(), branch.getName()));
                lastTime = Time.now();
            } else {
                lastTime = events.get(0).getSignature().getTime();
            }
        } else {
            Build build = builds.get(0);
            lastTime = build.getSignature().getTime();
        }
        // Logging
        infoListener.post(format("[%s][%s] Branch last build activity: %s", branch.getProject().getName(), branch.getName(), lastTime));
        // Deletion?
        if (deletionTime.isPresent() && deletionTime.get().compareTo(lastTime) > 0) {
            infoListener.post(format("[%s][%s] Branch due for deletion", branch.getProject().getName(), branch.getName()));
            structureService.deleteBranch(branch.getId());
        } else if (disablingTime.compareTo(lastTime) > 0 && !branch.isDisabled()) {
            infoListener.post(format("[%s][%s] Branch due for staleness - disabling", branch.getProject().getName(), branch.getName()));
            structureService.saveBranch(
                    branch.withDisabled(true)
            );
        } else {
            infoListener.post(format("[%s][%s] Not touching the branch", branch.getProject().getName(), branch.getName()));
        }
    }
}
