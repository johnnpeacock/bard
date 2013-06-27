package bard.db.context.item

import bard.db.audit.BardContextUtils
import bard.db.project.ProjectContextItem
import grails.plugin.spock.IntegrationSpec
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.hibernate.SessionFactory
import org.springframework.context.MessageSource
import spock.lang.Unroll

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 5/24/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Unroll
class ContextItemControllerIntegrationSpec extends IntegrationSpec {


    ContextItemController controller

    MessageSource messageSource

    ProjectContextItem contextItem
    SessionFactory sessionFactory

    void setup() {
        SpringSecurityUtils.reauthenticate('integrationTestUser', null)
        BardContextUtils.setBardContextUsername(sessionFactory.currentSession, 'integrationTestUser')
        controller = new ContextItemController()
    }

    void "test update number handling #desc"() {
        given:
        ProjectContextItem contextItem = ProjectContextItem.build()
        // unclear what is going on with the build method that requires a flush
        // after the save.
        // if the flush does not happen, then it occurs in the service call which causes a version mismatch
        ProjectContextItem.withSession { session ->
            session.flush()
        }

        setContextItemRelatedParams(contextItem)

        controller.params.attributeElementId = contextItem.attributeElement.id
        controller.params.valueNum = valueNumParam
        controller.params.qualifier = '= '

        when:
        controller.update()

        then:
        controller.modelAndView.model.instance.contextItem == contextItem
        controller.modelAndView.model.instance.valueNum == expectedValueNum

        where:
        desc | valueNumParam | expectedValueNum
        ''   | '3.5e-3'      | '0.0035'
        ''   | '3.5E-3'      | '0.0035'
        ''   | '0.0035'      | '0.0035'
        ''   | '0.0012'      | '0.0012'
        ''   | '0.1234567'   | '0.1234567'
        ''   | '0.3'         | '0.3'
        ''   | '0.000003'    | '0.000003'
        ''   | '0.000003'    | '0.000003'


    }

    void "test delete"() {
        given:
        ProjectContextItem contextItem = ProjectContextItem.build()
        final Long id = contextItem.id
        setContextItemRelatedParams(contextItem)

        when:
        controller.delete()

        then:
        ProjectContextItem.findById(id) == null

    }

    private void setContextItemRelatedParams(ProjectContextItem contextItem) {
        controller.params.contextOwnerId = contextItem.context?.owner?.id
        controller.params.contextId = contextItem.context?.id
        controller.params.contextItemId = contextItem.id
        controller.params.version = contextItem.version
        controller.params.contextClass = 'ProjectContext'
    }


}
