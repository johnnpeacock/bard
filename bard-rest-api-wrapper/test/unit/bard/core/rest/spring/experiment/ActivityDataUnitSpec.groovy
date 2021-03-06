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

package bard.core.rest.spring.experiment

import bard.core.rest.spring.DictionaryRestService
import bard.core.rest.spring.util.Node
import bard.rest.api.wrapper.Dummy
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ActivityDataUnitSpec extends Specification {
    @Shared
    ObjectMapper objectMapper = new ObjectMapper()

    @Shared
    String JSON_WITH_QUALIFIER = '''
    {
        "displayName":"IC50",
        "dictElemId":963,
        "responseUnit":"um",
        "testConcUnit":"uM",
        "value":"1.949",
        "qualifierValue":"="
    }
    '''
    @Shared
    String JSON_NO_QUALIFIER = '''
   {
        "displayName":"IC50",
        "dictElemId":963,
        "responseUnit":"um",
        "testConcUnit":"uM",
        "value":"1.949"

   }
   '''
    DictionaryRestService dictionaryRestService = Mock(DictionaryRestService)


    void "test JSON #label"() {
        given:
        Dummy dummy = new Dummy()
        dummy.dictionaryRestService = dictionaryRestService
        when:
        ActivityData activityData = objectMapper.readValue(JSON_DATA, ActivityData.class)
        activityData.dummy = dummy
        then:

        assert activityData
        assert activityData.displayName == "IC50"
        assert activityData.dictElemId == 963
        assert activityData.value == "1.949"
        assert activityData.responseUnit == "um"
        assert activityData.testConcentrationUnit == "uM"
        assert activityData.getDictionaryLabel() == "IC50"
        assert activityData.getDictionaryDescription() == null
        assert activityData.toDisplay().trim() == display.trim()
        assert activityData.qualifier == qualifier

        where:
        label                   | JSON_DATA           | hasQualifier | display             | qualifier
        "JSON Has qualifier"    | JSON_WITH_QUALIFIER | true         | "IC50 : =1.949 um " | "="
        "JSON Has no qualifier" | JSON_NO_QUALIFIER   | false        | "IC50 : 1.949 um "  | null
    }

    void "test toDisplay #label"() {
        given:
        ActivityData activityData = new ActivityData(testConcentration: testConcentration, responseUnit: responseUnit, qualifier: qualifier, testConcentrationUnit: "um", displayName: displayName)
        //activityData.metaClass.getDictionaryLabel = {dictionaryLabel}
        Dummy dummy = new Dummy()
        dummy.dictionaryRestService = dictionaryRestService
        activityData.dummy = dummy
        when:
        final String display = activityData.toDisplay()

        then:
        assert expectedDisplay.trim() == display.trim()
        where:
        label                         | qualifier | responseUnit | testConcentration | dictionaryLabel          | expectedDisplay                          | displayName
        "IC50"                        | ">"       | "um"         | null              | "IC50"                   | "IC50 : > um"                            | "IC50"
        "IC50 with testConcentration" | ""        | ""           | 223               | "IC50"                   | "IC50 :     Test Concentration:223.0 um" | "IC50"
        "Outcome"                     | ""        | ""           | 221               | "Outcome"                | "Test Concentration:221.0 um"            | ""
        "PubChem activity score"      | ""        | ""           | 1                 | "PubChem activity score" | "Test Concentration:1.0 um"              | ""
        "Activity_Score"              | ""        | ""           | 2                 | "Activity_Score"         | "Test Concentration:2.0 um"              | ""
        "Score"                       | ""        | ""           | 3                 | "Score"                  | "Test Concentration:3.0 um"              | ""

    }


    void "test getDictionaryDescription #label"() {
        given:
        ActivityData activityData = new ActivityData(dictElemId: dictElemId, displayName: "pubChem")
        Dummy dummy = new Dummy()
        dummy.dictionaryRestService = dictionaryRestService
        activityData.dummy = dummy
        when:
        final String foundDescription = activityData.getDictionaryDescription()

        then:
        expectedNumExecutions * dummy.dictionaryRestService.findDictionaryElementById(_) >> { dictionaryElement }
        assert expectedDescription == foundDescription
        where:
        label                                     | dictElemId | dictionaryElement              | expectedDescription | expectedNumExecutions
        "Has a DictElemId and in dictionary"      | 222        | new Node(description: "label") | "label"             | 1
        "Has a DictElemId, but not in dictionary" | 221        | null                           | null                | 1
        "Has no DictElemId"                       | 0          | null                           | null                | 0

    }

    void "test getDictionaryLabel #label"() {
        given:
        ActivityData activityData = new ActivityData(dictElemId: dictElemId, displayName: "pubChem")
        Dummy dummy = new Dummy()
        dummy.dictionaryRestService = dictionaryRestService
        activityData.dummy = dummy
        when:
        final String foundLabel = activityData.getDictionaryLabel()

        then:
        expectedNumExecutions * dummy.dictionaryRestService.findDictionaryElementById(_) >> { dictionaryElement }
        assert expectedLabel == foundLabel
        where:
        label                                     | dictElemId | dictionaryElement        | expectedLabel | expectedNumExecutions
        "Has a DictElemId and in dictionary"      | 222        | new Node(label: "label") | "label"       | 1
        "Has a DictElemId, but not in dictionary" | 221        | null                     | "pubChem"     | 1
        "Has no DictElemId"                       | 0          | null                     | "pubChem"     | 0

    }


}

