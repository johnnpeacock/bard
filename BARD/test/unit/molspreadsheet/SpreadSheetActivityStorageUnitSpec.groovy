/* Copyright (c) 2014, The Broad Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The Broad Institute nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL The Broad Institute BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package molspreadsheet

import bard.core.HillCurveValue
import bard.core.rest.spring.experiment.ConcentrationResponseSeries
import bard.core.rest.spring.experiment.PriorityElement
import bardqueryapi.ActivityOutcome
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll
import bard.core.rest.spring.experiment.CurveFitParameters
import bard.core.rest.spring.experiment.ConcentrationResponsePoint
import bard.core.rest.spring.experiment.ActivityData

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(MolSpreadSheetController)
@Unroll
class SpreadSheetActivityStorageUnitSpec extends Specification {

    MolSpreadSheetCell molSpreadSheetCell
    HillCurveValueHolder hillCurveValueHolder

    void setup() {
        molSpreadSheetCell = new MolSpreadSheetCell("0.123", MolSpreadSheetCellType.numeric)
        hillCurveValueHolder = new HillCurveValueHolder()
    }

    void tearDown() {
        // Tear down logic here
    }

    void "Smoke test can we build a spreadsheet activity storage data"() {
        when:
        SpreadSheetActivityStorage spreadSheetActivityStorage = new SpreadSheetActivityStorage()

        then:
        assertNotNull(spreadSheetActivityStorage)
    }





    void "Test print units "() {
        given:
        final SpreadSheetActivityStorage spreadSheetActivityStorage = new SpreadSheetActivityStorage(responseUnit: responseUnit)

        when:
        String printedUnit =  spreadSheetActivityStorage.printUnits(resultHolder)

        then:
        printedUnit == expectedPrintedUnit

        where:
        resultHolder        | expectedPrintedUnit   | responseUnit
        '--'                | ''                    | null
        ''                  | ''                    | null
        ''                  | '%'                   | 'percent'
        ''                  | 'uM'                  | 'um'
        ''                  | 'whatever'            | 'whatever'
    }




    void "Test constraints for molecular spreadsheet data"() {
        given:
        mockForConstraintsTests(SpreadSheetActivityStorage)

        when:
        SpreadSheetActivityStorage spreadSheetActivityStorage = new SpreadSheetActivityStorage()
        spreadSheetActivityStorage.setMolSpreadSheetCell(molSpreadSheetCell)

        then:
        spreadSheetActivityStorage.validate()
        !spreadSheetActivityStorage.hasErrors()
    }

    void "test hashCode all branches"() {
        given:
        SpreadSheetActivityStorage spreadSheetActivityStorage =
            new SpreadSheetActivityStorage(version: 2, id: 2,
                    molSpreadSheetCell: new MolSpreadSheetCell())
        when:
        final int hashCode = spreadSheetActivityStorage.hashCode()
        then:
        assert hashCode
    }
    /**
     * Demonstrate that we can go through 1000 values without encountering a duplicate hash code
     */
    void "Test hash code and demonstrate that it gives us a nice spread"() {
        when:
        List<SpreadSheetActivityStorage> activityStorageArrayList = []
        for (i in 1..10) {
            for (j in 1..10) {
                for (k in 1..10) {
                    SpreadSheetActivityStorage spreadSheetActivityStorage = new SpreadSheetActivityStorage()
                    spreadSheetActivityStorage.eid = i as Long
                    spreadSheetActivityStorage.cid = j as Long
                    spreadSheetActivityStorage.sid = k as Long
                    activityStorageArrayList << spreadSheetActivityStorage
                }
            }
        }
        Map<Integer, Integer> sheetActivityStorageIntegerLinkedHashMap = [:]
        for (SpreadSheetActivityStorage spreadSheetActivityStorage in activityStorageArrayList) {
            int hashCode = spreadSheetActivityStorage.hashCode()
            if (sheetActivityStorageIntegerLinkedHashMap.containsKey(hashCode)) {
                sheetActivityStorageIntegerLinkedHashMap[hashCode] = sheetActivityStorageIntegerLinkedHashMap[hashCode] + 1
            } else {
                sheetActivityStorageIntegerLinkedHashMap[hashCode] = 0

            }
        }

        then:
        for (Integer key in sheetActivityStorageIntegerLinkedHashMap.keySet()) {
            assert sheetActivityStorageIntegerLinkedHashMap[key] < 1
        }
    }

    void "Test constructor No HillCurve"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double

        when:
        SpreadSheetActivityStorage spreadSheetActivityStorage = new SpreadSheetActivityStorage(spreadSheetActivity)

        then:
        assertNotNull(spreadSheetActivityStorage)
        assert spreadSheetActivityStorage.sid == 1
        assert spreadSheetActivityStorage.activityOutcome == ActivityOutcome.ACTIVE
    }


    void "Test SpreadSheetActivityStorage constructor"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double
        final HillCurveValue hillCurveValue = new HillCurveValue()
        hillCurveValue.id = 1
        hillCurveValue.sinf = 1d
        hillCurveValue.s0 = 1d
        hillCurveValue.slope = 1d
        hillCurveValue.coef = 1d
        hillCurveValue.conc = [1d]
        hillCurveValue.response = [1d]

        when:
        SpreadSheetActivityStorage spreadSheetActivityStorage = new SpreadSheetActivityStorage(spreadSheetActivity)

        then:
        assertNotNull(spreadSheetActivityStorage)
        assert spreadSheetActivityStorage.sid == 1
        assert spreadSheetActivityStorage.activityOutcome == ActivityOutcome.ACTIVE
    }



    void "Test  MolSpreadSheetCell constructor in case of non-null child elements "() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double
        ActivityData activityData = new ActivityData()
        PriorityElement priorityElement = new PriorityElement(displayName: "testName", value: 0.47d, childElements: [activityData], concentrationResponseSeries: new ConcentrationResponseSeries())
        spreadSheetActivity.priorityElementList = [priorityElement,priorityElement]

        when:
        MolSpreadSheetCell molSpreadSheetCell =  new  MolSpreadSheetCell(spreadSheetActivity)

        then:
        assertNotNull(molSpreadSheetCell)
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList.size()==2
    }






    void "Test  MolSpreadSheetCell constructor in case of multiple identical column names"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double
        PriorityElement priorityElement = new PriorityElement(displayName: "testName", value: 0.47d, childElements: [], concentrationResponseSeries: new ConcentrationResponseSeries())
        spreadSheetActivity.priorityElementList = [priorityElement,priorityElement]

        when:
        MolSpreadSheetCell molSpreadSheetCell =  new  MolSpreadSheetCell(spreadSheetActivity)

        then:
        assertNotNull(molSpreadSheetCell)
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList.size()==2
    }




    void "Test  MolSpreadSheetCell constructor a blank value"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double
        PriorityElement priorityElement = new PriorityElement(displayName: "testName", value: "")
        spreadSheetActivity.priorityElementList = [priorityElement]

        when:
        MolSpreadSheetCell molSpreadSheetCell =  new  MolSpreadSheetCell(spreadSheetActivity)

        then:
        assertNotNull(molSpreadSheetCell)
        molSpreadSheetCell.activity ==  MolSpreadSheetCellActivityOutcome.Unspecified
        molSpreadSheetCell.toString() == null
    }




    void "Test  MolSpreadSheetCell constructor a null value"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.INACTIVE
        spreadSheetActivity.potency = 3 as Double
        PriorityElement priorityElement = new PriorityElement(displayName: "testName", value: null,childElements: [], concentrationResponseSeries: new ConcentrationResponseSeries())
        spreadSheetActivity.priorityElementList = [priorityElement]

        when:
        MolSpreadSheetCell molSpreadSheetCell =  new  MolSpreadSheetCell(spreadSheetActivity)

        then:
        assertNotNull(molSpreadSheetCell)
        molSpreadSheetCell.activity ==  MolSpreadSheetCellActivityOutcome.Unknown
        assertNotNull molSpreadSheetCell.spreadSheetActivityStorage
        assertNotNull molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList.size()==1
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList[0].toString()=="--"
    }




    void "Test  MolSpreadSheetCell constructor in case of no value"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double
        PriorityElement priorityElement = new PriorityElement(displayName: "testName", childElements: [], concentrationResponseSeries: new ConcentrationResponseSeries())
        spreadSheetActivity.priorityElementList = [priorityElement]

        when:
        MolSpreadSheetCell molSpreadSheetCell =  new  MolSpreadSheetCell(spreadSheetActivity)

        then:
        assertNotNull(molSpreadSheetCell)
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList.size()==1
    }


    void "Test  MolSpreadSheetCell constructor in case of no displayName"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double
        PriorityElement priorityElement = new PriorityElement(value: 0.47d, childElements: [], concentrationResponseSeries: new ConcentrationResponseSeries())
        priorityElement.responseUnit='nM'
        spreadSheetActivity.priorityElementList = [priorityElement]

        when:
        MolSpreadSheetCell molSpreadSheetCell =  new  MolSpreadSheetCell(spreadSheetActivity)

        then:
        assertNotNull(molSpreadSheetCell)
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList.size()==1
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList[0].identifier=='nM'
    }
    void "Test  MolSpreadSheetCell constructor in case of no displayName and no response unit"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double
        PriorityElement priorityElement = new PriorityElement(value: 0.47d, childElements: [], concentrationResponseSeries: new ConcentrationResponseSeries())
        spreadSheetActivity.priorityElementList = [priorityElement]

        when:
        MolSpreadSheetCell molSpreadSheetCell =  new  MolSpreadSheetCell(spreadSheetActivity)

        then:
        assertNotNull(molSpreadSheetCell)
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList.size()==1
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList[0].identifier==' '
    }



    void "Test  MolSpreadSheetCell constructor with curve parms"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double
        PriorityElement priorityElement = new PriorityElement(value: 0.47d, displayName: "colname")
        ConcentrationResponseSeries concentrationResponseSeries = new ConcentrationResponseSeries()
        concentrationResponseSeries.concentrationResponsePoints = []
        concentrationResponseSeries.concentrationResponsePoints  << new ConcentrationResponsePoint(testConcentration: 0.1d, value: 0.1d)
        concentrationResponseSeries.concentrationResponsePoints  << new ConcentrationResponsePoint(testConcentration: 0.2d, value: 0.2d)
        CurveFitParameters curveFitParameters = new CurveFitParameters(s0: 1.0d,
                sInf: 1.0d,
                hillCoef: 1.0d)
        concentrationResponseSeries.curveFitParameters =  curveFitParameters
        priorityElement.concentrationResponseSeries = concentrationResponseSeries
        spreadSheetActivity.priorityElementList = [priorityElement]

        when:
        MolSpreadSheetCell molSpreadSheetCell =  new  MolSpreadSheetCell(spreadSheetActivity)

        then:
        assertNotNull(molSpreadSheetCell)
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList.size()==1
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList[0].identifier=='colname'
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList[0].s0==1.0d
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList[0].conc.size()==2
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList[0].response.size()==2
    }




    void "Test  MolSpreadSheetCell constructor in case of one column names"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double
        PriorityElement priorityElement = new PriorityElement(displayName: "testName", value: 0.47d, childElements: [], concentrationResponseSeries: new ConcentrationResponseSeries())
        spreadSheetActivity.priorityElementList = [priorityElement]

        when:
        MolSpreadSheetCell molSpreadSheetCell =  new  MolSpreadSheetCell(spreadSheetActivity)

        then:
        assertNotNull(molSpreadSheetCell)
        molSpreadSheetCell.spreadSheetActivityStorage.hillCurveValueHolderList.size()==1
    }



    void "Test  MolSpreadSheetCell constructor with qualifiers"() {
        given:
        final SpreadSheetActivity spreadSheetActivity = new SpreadSheetActivity()
        spreadSheetActivity.sid = 1 as Long
        spreadSheetActivity.activityOutcome = ActivityOutcome.ACTIVE
        spreadSheetActivity.potency = 3 as Double
        PriorityElement priorityElement = new PriorityElement(displayName: "testName", value: 0.47d, childElements: [], concentrationResponseSeries: new ConcentrationResponseSeries())
        spreadSheetActivity.priorityElementList = [priorityElement]


        when:
        priorityElement.qualifier = qualifier
        MolSpreadSheetCell molSpreadSheetCell =  new  MolSpreadSheetCell(spreadSheetActivity)

        then:
        assertNotNull(molSpreadSheetCell)
        molSpreadSheetCell.molSpreadSheetCellType ==  TypeOfMolSpreadSheetCell

        where:
        qualifier       | TypeOfMolSpreadSheetCell
        ">"             | MolSpreadSheetCellType.greaterThanNumeric
        "<"             | MolSpreadSheetCellType.lessThanNumeric
        "="             | MolSpreadSheetCellType.numeric
    }




    void "Test equals"() {
        given:
        final SpreadSheetActivityStorage spreadSheetActivityStorage1 = new SpreadSheetActivityStorage(eid: 47l, cid: 47l, sid: 47l)


        when:
        SpreadSheetActivityStorage spreadSheetActivityStorage2 = new SpreadSheetActivityStorage()
        spreadSheetActivityStorage2.eid = eid
        spreadSheetActivityStorage2.cid = cid
        spreadSheetActivityStorage2.sid = sid

        then:
        spreadSheetActivityStorage2.equals(spreadSheetActivityStorage1) == equality

        where:
        eid        | cid        | sid        | equality
        48 as Long | 47 as Long | 47 as Long | false
        47 as Long | 48 as Long | 47 as Long | false
        47 as Long | 47 as Long | 48 as Long | false
        47 as Long | 47 as Long | 47 as Long | true

    }

    void "Test extended equals #label"() {
        when:
        final boolean equals = spreadSheetActivityStorage.equals(otherSpreadSheetActivityStorage)

        then:
        equals == equality
        where:
        label               | spreadSheetActivityStorage       | otherSpreadSheetActivityStorage | equality
        "Other is null"     | new SpreadSheetActivityStorage() | null                            | false
        "Different classes" | new SpreadSheetActivityStorage() | 20                              | false

    }

}
