package net.nemerosa.ontrack.extension.github.property;

import net.nemerosa.ontrack.extension.git.model.GitConfiguration;
import net.nemerosa.ontrack.extension.github.GitHubIssueServiceExtension;
import net.nemerosa.ontrack.extension.issues.model.IssueServiceConfigurationIdentifier;
import net.nemerosa.ontrack.model.support.UserPassword;

import java.util.Optional;

import static java.lang.String.format;

public class GitHubGitConfiguration implements GitConfiguration {

    public static final String CONFIGURATION_REPOSITORY_SEPARATOR = ":";
    private final GitHubProjectConfigurationProperty property;

    public GitHubGitConfiguration(GitHubProjectConfigurationProperty property) {
        this.property = property;
    }

    @Override
    public String getType() {
        return "github";
    }

    @Override
    public String getName() {
        return property.getConfiguration().getName();
    }

    @Override
    public String getRemote() {
        return format(
                "%s/%s.git",
                property.getConfiguration().getUrl(),
                property.getRepository()
        );
    }

    @Override
    public Optional<UserPassword> getCredentials() {
        return property.getConfiguration().getCredentials();
    }

    @Override
    public String getCommitLink() {
        return format(
                "%s/%s/commit/{commit}",
                property.getConfiguration().getUrl(),
                property.getRepository()
        );
    }

    @Override
    public String getFileAtCommitLink() {
        return format(
                "%s/%s/blob/{commit}/{path}",
                property.getConfiguration().getUrl(),
                property.getRepository()
        );
    }

    @Override
    public int getIndexationInterval() {
        return property.getIndexationInterval();
    }

    /**
     * The returned identifier combines the service id (GitHub) with the configuration name
     * and the repository name.
     * <p>
     * The syntax is:
     * <p>
     * <code>github//configuration:repository</code>
     *
     * @see IssueServiceConfigurationIdentifier
     */
    @Override
    public String getIssueServiceConfigurationIdentifier() {
        return new IssueServiceConfigurationIdentifier(
                GitHubIssueServiceExtension.GITHUB_SERVICE_ID,
                property.getConfiguration().getName()
                        + CONFIGURATION_REPOSITORY_SEPARATOR
                        + property.getRepository()
        ).format();
    }
}
