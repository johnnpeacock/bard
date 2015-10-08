
grails.release.scm.enabled = false

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    //log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        inherits true // Whether to inherit repository definitions from plugins
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    plugins {
        compile(":spock:0.7") {
            exclude "spock-grails-support"
        }
        build ":release:2.0.3", {export = false}
    }

    dependencies {
        compile "org.spockframework:spock-grails-support:0.7-groovy-2.0"

    }
}
