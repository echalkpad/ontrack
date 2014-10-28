package net.nemerosa.ontrack.acceptance

import org.junit.Ignore
import org.junit.Test

import static net.nemerosa.ontrack.json.JsonUtils.object

class ACCStructure extends AcceptanceTestClient {

    @Test
    void 'No name for a project is invalid'() {
        validationMessage({ doCreateProject(object().end()) }, "The name is required.")
    }

    @Test
    @Ignore
    void 'Empty name for a project is invalid'() {
        validationMessage({
            doCreateProject(object().with('name', '').end())
        }, 'The name can only have letters, digits, dots (.), dashes (-) or underscores (_).')
    }

    @Test
    void 'Name validation for a build (correct)'() {
        def branch = doCreateBranch()
        def build = doCreateBuild(branch.path('id').asInt(), object().with('name', '2.0.0-alpha-1-14').with('description', '').end())
        assert build.path('id').asInt() > 0
    }

    @Test
    void 'Name validation for a branch (correct)'() {
        def project = doCreateProject()
        def branch = doCreateBranch(project.path('id').asInt(), object().with('name', '2.0.0-alpha-x').with('description', '').end())
        assert branch.path('id').asInt() > 0
    }

    @Test
    void 'Name validation for a project (correct)'() {
        def project = doCreateProject(nameDescription())
        assert project.path('id').asInt() > 0
    }

}
