package net.nemerosa.ontrack.extension.git.service

import net.nemerosa.ontrack.extension.git.model.ConfiguredBuildGitCommitLink
import net.nemerosa.ontrack.extension.git.model.GitConfiguration
import net.nemerosa.ontrack.extension.git.property.GitBranchConfigurationProperty
import net.nemerosa.ontrack.extension.git.property.GitBranchConfigurationPropertyType
import net.nemerosa.ontrack.extension.git.property.GitProjectConfigurationProperty
import net.nemerosa.ontrack.extension.git.property.GitProjectConfigurationPropertyType
import net.nemerosa.ontrack.extension.git.support.TagPattern
import net.nemerosa.ontrack.extension.git.support.TagPatternBuildNameGitCommitLink
import net.nemerosa.ontrack.model.structure.*
import org.junit.Test

import static net.nemerosa.ontrack.model.structure.NameDescription.nd
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class DefaultGitConfiguratorTest {

    @Test
    void 'Without project config and without branch config'() {
        Project project = Project.of(nd('P', "Project")).withId(ID.of(1))
        Branch branch = Branch.of(project, nd('1.2', "Branch 1.2")).withId(ID.of(2))

        GitConfigurationService gitConfigurationService = mock(GitConfigurationService)

        BuildGitCommitLinkService buildGitCommitLinkService = mock(BuildGitCommitLinkService)

        PropertyService propertyService = mock(PropertyService)
        when(propertyService.getProperty(project, GitProjectConfigurationPropertyType)).thenReturn(
                Property.of(
                        new GitProjectConfigurationPropertyType(gitConfigurationService),
                        null
                )
        )
        when(propertyService.getProperty(branch, GitBranchConfigurationPropertyType)).thenReturn(
                Property.of(
                        new GitBranchConfigurationPropertyType(buildGitCommitLinkService),
                        null
                )
        )

        DefaultGitConfigurator configurator = new DefaultGitConfigurator(propertyService, buildGitCommitLinkService)

        def configuration = configurator.configure(GitConfiguration.empty(), branch)

        assert configuration != null
        assert configuration.branch == 'master'
        assert configuration.buildCommitLink.link.id == 'tag'
    }

    @Test
    void 'Without project config and with branch config'() {
        Project project = Project.of(nd('P', "Project")).withId(ID.of(1))
        Branch branch = Branch.of(project, nd('1.2', "Branch 1.2")).withId(ID.of(2))

        GitConfigurationService gitConfigurationService = mock(GitConfigurationService)

        BuildGitCommitLinkService buildGitCommitLinkService = mock(BuildGitCommitLinkService)
        when(buildGitCommitLinkService.getLink("tagPattern")).thenReturn(new TagPatternBuildNameGitCommitLink())

        PropertyService propertyService = mock(PropertyService)
        when(propertyService.getProperty(project, GitProjectConfigurationPropertyType)).thenReturn(
                Property.of(
                        new GitProjectConfigurationPropertyType(gitConfigurationService),
                        null
                )
        )
        when(propertyService.getProperty(branch, GitBranchConfigurationPropertyType)).thenReturn(
                Property.of(
                        new GitBranchConfigurationPropertyType(buildGitCommitLinkService),
                        new GitBranchConfigurationProperty(
                                '2.1',
                                new ConfiguredBuildGitCommitLink<>(
                                        new TagPatternBuildNameGitCommitLink(),
                                        new TagPattern("2.1.*")
                                ).toServiceConfiguration(),
                                true,
                                0
                        )
                )
        )

        DefaultGitConfigurator configurator = new DefaultGitConfigurator(propertyService, buildGitCommitLinkService)

        def configuration = configurator.configure(GitConfiguration.empty(), branch)

        assert configuration != null
        assert configuration.branch == '2.1'
        assert configuration.buildCommitLink.link.id == 'tagPattern'
        assert configuration.buildCommitLink.data == new TagPattern("2.1.*")
    }

    @Test
    void 'With project config but without branch config'() {
        Project project = Project.of(nd('P', "Project")).withId(ID.of(1))
        Branch branch = Branch.of(project, nd('1.2', "Branch 1.2")).withId(ID.of(2))

        GitConfigurationService gitConfigurationService = mock(GitConfigurationService)

        BuildGitCommitLinkService buildGitCommitLinkService = mock(BuildGitCommitLinkService)

        GitConfiguration gitConfiguration = GitConfiguration.empty()

        PropertyService propertyService = mock(PropertyService)
        when(propertyService.getProperty(project, GitProjectConfigurationPropertyType)).thenReturn(
                Property.of(
                        new GitProjectConfigurationPropertyType(gitConfigurationService),
                        new GitProjectConfigurationProperty(
                                gitConfiguration
                        )
                )
        )
        when(propertyService.getProperty(branch, GitBranchConfigurationPropertyType)).thenReturn(
                Property.of(
                        new GitBranchConfigurationPropertyType(buildGitCommitLinkService),
                        null
                )
        )

        DefaultGitConfigurator configurator = new DefaultGitConfigurator(propertyService, buildGitCommitLinkService)

        def configuration = configurator.configure(GitConfiguration.empty(), branch)

        assert configuration != null
        assert configuration.branch == 'master'
        assert configuration.buildCommitLink.link.id == 'tag'
    }

    @Test
    void 'With project and branch config'() {
        Project project = Project.of(nd('P', "Project")).withId(ID.of(1))
        Branch branch = Branch.of(project, nd('1.2', "Branch 1.2")).withId(ID.of(2))

        GitConfigurationService gitConfigurationService = mock(GitConfigurationService)

        BuildGitCommitLinkService buildGitCommitLinkService = mock(BuildGitCommitLinkService)
        when(buildGitCommitLinkService.getLink("tagPattern")).thenReturn(new TagPatternBuildNameGitCommitLink())

        GitConfiguration gitConfiguration = GitConfiguration.empty()

        PropertyService propertyService = mock(PropertyService)
        when(propertyService.getProperty(project, GitProjectConfigurationPropertyType)).thenReturn(
                Property.of(
                        new GitProjectConfigurationPropertyType(gitConfigurationService),
                        new GitProjectConfigurationProperty(
                                gitConfiguration
                        )
                )
        )
        when(propertyService.getProperty(branch, GitBranchConfigurationPropertyType)).thenReturn(
                Property.of(
                        new GitBranchConfigurationPropertyType(buildGitCommitLinkService),
                        new GitBranchConfigurationProperty(
                                '2.1',
                                new ConfiguredBuildGitCommitLink<>(
                                        new TagPatternBuildNameGitCommitLink(),
                                        new TagPattern("2.1.*")
                                ).toServiceConfiguration(),
                                true,
                                0
                        )
                )
        )

        DefaultGitConfigurator configurator = new DefaultGitConfigurator(propertyService, buildGitCommitLinkService)

        def configuration = configurator.configure(GitConfiguration.empty(), branch)

        assert configuration != null
        assert configuration.branch == '2.1'
        assert configuration.buildCommitLink.link.id == 'tagPattern'
        assert configuration.buildCommitLink.data == new TagPattern("2.1.*")
    }

}