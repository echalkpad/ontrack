node('ontrack') {

    stage 'Build'

    git credentialsId: 'jenkins', url: 'https://github.com/nemerosa/ontrack.git', branch: env.BRANCH_NAME
    // Builds the Docker image used for the build
    def image = docker.build("nemerosa/ontrack-build:${env.BRANCH_NAME.replaceAll('/', '-')}", 'seed/docker')
    // Docker run arguments
    def runArgs = '''\
        --volume=/root/.gradle:/root/.gradle \
        --volume=/root/.cache:/root/.cache \
        --volume=/var/run/docker.sock:/var/run/docker.sock \
        --net=host
        '''
    // Runs the build inside the Docker image
    image.inside(runArgs) {
        // Unit tests and package
        try {
            sh '''\
                ./gradlew \
                    clean \
                    versionDisplay \
                    versionFile \
                    test \
                    integrationTest \
                    osPackages \
                    dockerLatest \
                    build \
                    --info \
                    --stacktrace \
                    --profile \
                    --console plain \
                    --no-daemon
                '''
            archive includes: '''\
                build/distributions/ontrack-*-delivery.zip,\
                build/distributions/ontrack*.deb,\
                build/distributions/ontrack*.rpm\
            '''
        } finally {
            step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/*.xml'])
        }
    }

    def versionInfo = readProperties file: 'build/version.properties'
    env.VERSION_DISPLAY = versionInfo.VERSION_DISPLAY
    echo "Version = ${env.VERSION_DISPLAY}"

    stage 'Local acceptance'
    image.inside(runArgs) {
        try {
            wrap([$class: 'Xvfb', installationName: 'default', displayName: '2']) {
                sh '''\
                    ./gradlew \
                        ciAcceptanceTest \
                        -PacceptanceJar=ontrack-acceptance/build/libs/ontrack-acceptance-${VERSION_DISPLAY}.jar \
                        --info \
                        --profile \
                        --stacktrace \
                        --console plain \
                        --no-daemon
                '''
            }
            archive includes: 'build/ci-ontrack.log'
        } finally {
            step([$class: 'JUnitResultArchiver', testResults: '*-tests.xml'])
        }
    }
}
