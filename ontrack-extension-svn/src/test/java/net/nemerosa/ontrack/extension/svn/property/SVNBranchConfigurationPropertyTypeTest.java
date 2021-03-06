package net.nemerosa.ontrack.extension.svn.property;

import net.nemerosa.ontrack.extension.scm.SCMExtensionFeature;
import net.nemerosa.ontrack.extension.svn.SVNExtensionFeature;
import net.nemerosa.ontrack.extension.svn.model.BuildSvnRevisionLinkService;
import net.nemerosa.ontrack.extension.svn.service.SVNService;
import net.nemerosa.ontrack.extension.svn.support.TagNameSvnRevisionLink;
import net.nemerosa.ontrack.model.security.ProjectConfig;
import net.nemerosa.ontrack.model.security.SecurityService;
import net.nemerosa.ontrack.model.structure.*;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SVNBranchConfigurationPropertyTypeTest {

    @Test
    public void not_editable_when_project_not_configured() {
        Project project = Project.of(new NameDescription("P", "Project")).withId(ID.of(1));
        Branch branch = Branch.of(project, new NameDescription("B", "Branch")).withId(ID.of(1));

        PropertyService propertiesService = mock(PropertyService.class);
        when(propertiesService.hasProperty(project, SVNProjectConfigurationPropertyType.class)).thenReturn(false);

        SecurityService securityService = mock(SecurityService.class);
        when(securityService.isProjectFunctionGranted(1, ProjectConfig.class)).thenReturn(true);

        BuildSvnRevisionLinkService buildSvnRevisionLinkService = mock(BuildSvnRevisionLinkService.class);

        TagNameSvnRevisionLink tagNameSvnRevisionLink = new TagNameSvnRevisionLink(
                mock(SVNService.class),
                mock(StructureService.class));

        SVNBranchConfigurationPropertyType propertyType = new SVNBranchConfigurationPropertyType(
                new SVNExtensionFeature(new SCMExtensionFeature()),
                propertiesService,
                buildSvnRevisionLinkService,
                tagNameSvnRevisionLink
        );

        assertFalse(propertyType.canEdit(branch, securityService));
    }

    @Test
    public void editable_when_project_configured() {
        Project project = Project.of(new NameDescription("P", "Project")).withId(ID.of(1));
        Branch branch = Branch.of(project, new NameDescription("B", "Branch")).withId(ID.of(1));

        PropertyService propertiesService = mock(PropertyService.class);
        when(propertiesService.hasProperty(project, SVNProjectConfigurationPropertyType.class)).thenReturn(true);

        SecurityService securityService = mock(SecurityService.class);
        when(securityService.isProjectFunctionGranted(1, ProjectConfig.class)).thenReturn(true);

        BuildSvnRevisionLinkService buildSvnRevisionLinkService = mock(BuildSvnRevisionLinkService.class);

        TagNameSvnRevisionLink tagNameSvnRevisionLink = new TagNameSvnRevisionLink(
                mock(SVNService.class),
                mock(StructureService.class));

        SVNBranchConfigurationPropertyType propertyType = new SVNBranchConfigurationPropertyType(
                new SVNExtensionFeature(new SCMExtensionFeature()),
                propertiesService,
                buildSvnRevisionLinkService,
                tagNameSvnRevisionLink);

        assertTrue(propertyType.canEdit(branch, securityService));
    }

}
