node('ontrack') {
    stage 'Commit'
    git credentialsId: 'jenkins', url: 'https://github.com/nemerosa/ontrack.git', branch: 'feature/slave'
    // Builds the Docker image used for the build
    def image = docker.build('nemerosa/ontrack-build', 'seed/docker')
    // Docker run arguments
    def runArgs = '''\
        --volume=/root/.gradle:/root/.gradle \
        --volume=/root/.cache:/root/.cache \
        '''
    // Runs the build inside the Docker image
    image.inside(runArgs) {
        try {
            sh '''\
                ./gradlew \
                    clean \
                    versionDisplay \
                    versionFile \
                    test \
                    integrationTest \
                    osPackages \
                    build \
                    --info \
                    --stacktrace \
                    --profile \
                    --console plain \
                    --no-daemon
                '''
            step([$class: 'ArtifactArchiver', artifacts: '''\
                build/distributions/ontrack-*-delivery.zip,\
                build/distributions/ontrack*.deb,\
                build/distributions/ontrack*.rpm\
                '''])
        } finally {
            step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/*.xml'])
        }
    }
}