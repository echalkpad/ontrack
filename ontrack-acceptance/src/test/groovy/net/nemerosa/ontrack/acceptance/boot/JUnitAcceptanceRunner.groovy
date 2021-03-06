package net.nemerosa.ontrack.acceptance.boot

import net.nemerosa.ontrack.acceptance.support.AcceptanceTest
import net.nemerosa.ontrack.acceptance.support.AcceptanceTestSuite
import org.junit.runner.JUnitCore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class JUnitAcceptanceRunner implements AcceptanceRunner {

    private final Logger logger = LoggerFactory.getLogger(JUnitAcceptanceRunner)
    private final ApplicationContext applicationContext
    private final AcceptanceConfig config

    @Autowired
    JUnitAcceptanceRunner(AcceptanceConfig config, ApplicationContext applicationContext) {
        this.config = config
        this.applicationContext = applicationContext
    }

    @Override
    boolean run() throws Exception {
        logger.info "Starting acceptance tests."
        logger.info "Config URL    : ${config.url}"
        logger.info "Config context: ${config.context}"
        logger.info "Disabling SSL : ${config.disableSsl}"

        // Config as system properties
        config.setSystemProperties()

        // JUnit runtime
        JUnitCore junit = new JUnitCore()

        // XML reporting
        XMLRunListener xmlRunListener = new XMLRunListener(System.out)
        junit.addListener(xmlRunListener)

        // Gets all the acceptance suites
        def suites = applicationContext.getBeansWithAnnotation(AcceptanceTestSuite)

        // Filters on classes
        suites = suites.findAll { name, bean ->
            def acceptanceTest = applicationContext.findAnnotationOnBean(name, AcceptanceTest)
            return config.acceptTest(acceptanceTest)
        }

        // Class names
        def classes = suites.values().collect { it.class }

        // Creates the runners
        def runners = classes.collect { new AcceptanceTestRunner(it, config) }

        // Running the tests
        boolean ok = runners
                .collect { junit.run(it) }
                .every { it.wasSuccessful() }

        // XML output
        xmlRunListener.render(new File(config.resultFile))

        // Result
        ok

    }
}
