package org.codehaus.groovy.grails.web.context

import org.grails.web.servlet.context.GrailsConfigUtils

import javax.servlet.ServletContext

import grails.util.Environment

import grails.core.DefaultGrailsApplication
import grails.core.GrailsApplication
import org.grails.web.servlet.context.support.GrailsRuntimeConfigurator
import org.grails.support.MockApplicationContext
import org.springframework.context.ApplicationContext
import org.springframework.mock.web.MockServletContext

/**
 * @author Graeme Rocher
 * @since 1.2
 */
class GrailsConfigUtilsTests extends GroovyTestCase {

    void testExecuteBootstraps() {
        def app = new DefaultGrailsApplication([SimpleBootStrap] as Class[], getClass().classLoader)
        app.initialise()
        def ctx = new MockApplicationContext()
        def servletContext = new MockServletContext()
        GrailsConfigUtils.executeGrailsBootstraps(app, ctx, servletContext)

        assertEquals "bar", servletContext.getAttribute("foo")
    }

    void testExecuteEnvironmentSpecificBootstraps() {
        System.setProperty("grails.env", "dev")

        def app = new DefaultGrailsApplication([EnvironmentSpecificBootStrap] as Class[], getClass().classLoader)
        app.initialise()
        def ctx = new MockApplicationContext()
        def servletContext = new MockServletContext()
        GrailsConfigUtils.executeGrailsBootstraps(app, ctx, servletContext)

        assertEquals "bar", servletContext.getAttribute("foo")
        assertEquals "dev", servletContext.getAttribute("env")
    }

    void testDerivedGrailsConfigurator() {
        def app = new DefaultGrailsApplication([SimpleBootStrap] as Class[], getClass().classLoader)
        app.initialise()
        def ctx = new MockApplicationContext()
        def servletContext = new MockServletContext()
        servletContext.addInitParameter("grailsConfiguratorClass", MyGrailsRuntimeConfigurator.class.getName())

        def configurator = GrailsConfigUtils.determineGrailsRuntimeConfiguratorFromServletContext(app, servletContext, ctx)

        assertNotNull configurator
        assertEquals configurator.class.name, MyGrailsRuntimeConfigurator.class.name
    }

    void testNonExistingGrailsConfigurator() {
        def app = new DefaultGrailsApplication([SimpleBootStrap] as Class[], getClass().classLoader)
        app.initialise()
        def ctx = new MockApplicationContext()
        def servletContext = new MockServletContext()
        servletContext.addInitParameter("grailsConfiguratorClass", "org.codehaus.groovy.grails.web.context.ClassDoesNotExist")

        shouldFail(IllegalArgumentException) {
            def configurator = GrailsConfigUtils.determineGrailsRuntimeConfiguratorFromServletContext(app, servletContext, ctx)
        }
    }

    void testDefaultGrailsConfigurator() {
        def app = new DefaultGrailsApplication([SimpleBootStrap] as Class[], getClass().classLoader)
        app.initialise()
        def ctx = new MockApplicationContext()
        def servletContext = new MockServletContext()

        def configurator = GrailsConfigUtils.determineGrailsRuntimeConfiguratorFromServletContext(app, servletContext, ctx)

        assertNull configurator
    }

    protected void tearDown() {
        System.setProperty(Environment.KEY, "")
    }
}

class SimpleBootStrap {
    def init = { ServletContext ctx -> ctx.setAttribute("foo", "bar") }
}

class EnvironmentSpecificBootStrap {
    def init = { ServletContext ctx ->
        environments {
            production {
                ctx.setAttribute("env", "prod")
            }
            development {
                ctx.setAttribute("env", "dev")
            }
        }
        ctx.setAttribute("foo", "bar")
    }

    def destroy = {}
}

class MyGrailsRuntimeConfigurator extends GrailsRuntimeConfigurator {

    MyGrailsRuntimeConfigurator(GrailsApplication application, ApplicationContext parent) {
        super(application, parent)
    }

    MyGrailsRuntimeConfigurator(GrailsApplication application) {
        super(application)
    }
}
