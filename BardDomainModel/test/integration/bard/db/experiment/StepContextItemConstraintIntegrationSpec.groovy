package bard.db.experiment

import org.junit.Before

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 8/23/12
 * Time: 12:42 AM
 * To change this template use File | Settings | File Templates.
 */
class StepContextItemConstraintIntegrationSpec extends AbstractContextItemIntegrationSpec {

    @Before
    @Override
    void doSetup() {
        domainInstance = StepContextItem.buildWithoutSave()
        domainInstance.attributeElement.save()
    }
}
