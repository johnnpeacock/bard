package bard.db.registration

import bard.db.audit.BardContextUtils
import bard.db.enums.AssayStatus
import bard.db.enums.AssayType
import grails.plugin.spock.IntegrationSpec
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.acl.AclSid
import org.grails.plugins.springsecurity.service.acl.AclUtilService
import org.hibernate.SessionFactory
import org.junit.Before
import org.springframework.security.acls.model.Sid
import spock.lang.IgnoreRest
import spock.lang.Unroll

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 8/21/12
 * Time: 9:18 AM
 * To change this template use File | Settings | File Templates.
 */
@Unroll
class AssayDefinitionServiceIntegrationSpec extends IntegrationSpec {

    AssayDefinitionService assayDefinitionService
    SessionFactory sessionFactory
    AclUtilService aclUtilService

    @Before
    void setup() {
        BardContextUtils.setBardContextUsername(sessionFactory.currentSession, 'test')
        SpringSecurityUtils.reauthenticate('integrationTestUser', null)
    }

    void "test update designed By"() {
        given:
        final Assay assay = Assay.build(assayName: 'assayName20', designedBy: "BARD")
        final String newDesignedBy = "CAP"
        when:
        final Assay updatedAssay = assayDefinitionService.updateDesignedBy(assay.id, newDesignedBy)
        then:
        assert newDesignedBy == updatedAssay.designedBy
    }

    void "test update assay name"() {
        given:
        final Assay assay = Assay.build(assayName: 'assayName20', assayStatus: AssayStatus.DRAFT)
        final String newAssayName = "New Assay Name"
        when:
        final Assay updatedAssay = assayDefinitionService.updateAssayName(assay.id, newAssayName)
        then:
        assert newAssayName == updatedAssay.assayName
    }

    void "test update assay type"() {
        given:
        final Assay assay = Assay.build(assayName: 'assayName10', assayType: AssayType.PANEL_GROUP)
        when:
        final Assay updatedAssay = assayDefinitionService.updateAssayType(assay.id, AssayType.TEMPLATE)
        then:
        assert AssayType.TEMPLATE == updatedAssay.assayType
    }

    void "test save new Assay"() {
        given:
        final Assay assay = Assay.build(assayName: 'assayName40', assayType: AssayType.PANEL_GROUP)
        when:
        final Assay updatedAssay = assayDefinitionService.saveNewAssay(assay)
        then:
        assert AssayType.PANEL_GROUP == updatedAssay.assayType
    }
}