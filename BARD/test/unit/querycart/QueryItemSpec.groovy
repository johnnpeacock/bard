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

package querycart

import grails.test.mixin.TestFor
import spock.lang.Unroll
import spock.lang.Specification
import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.StringUtils

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(QueryItem)
@Unroll
class QueryItemSpec extends Specification {

    void "test toString #label"() {
        given:
        QueryItem item = new QueryItem(name, 1, 5, QueryItemType.Project)

        when:
        String itemAsString = item.toString()

        then:
        assert itemAsString == expectedName

        where:
        label                 | name         | expectedName
        "Empty Name"          | ""           | ""
        "With Name"           | "Some Title" | "Some Title"
        "Null Name"           | null         | ""

    }

    void "Test equals and hashCode #label"() {
        given:
        QueryItem queryItem1 = new QueryItem(name1, id1, id1, type1)
        QueryItem queryItem2 = new QueryItem(name2, id2, id2, type2)

        when:
        int code1 = queryItem1.hashCode()
        int code2 = queryItem2.hashCode()

        then:
        assert queryItem1 != new BigDecimal(1) // check that different classes are not equal
        assert !queryItem1.equals(null) // check that we don't get a NPE
        assert (code1 == code2) == equalityExpectation
        assert (queryItem1 == queryItem2) == equalityExpectation

        where:
        label                   | name1  | id1  | type1                 | name2| id2  | type2                  | equalityExpectation
        "Empty classes"         | null   | null | null                  | null | null | null                   | true
        "all different"         | "A"    | 1    | QueryItemType.Project | "B"  | 2    | QueryItemType.Compound | false
        "All same"              | "A"    | 1    | QueryItemType.Project | "A"  | 1    | QueryItemType.Project  | true
        "Diff ids"              | "A"    | 1    | QueryItemType.Project | "A"  | 2    | QueryItemType.Project  | false
        "Diff names"            | "A"    | 1    | QueryItemType.Project | "B"  | 1    | QueryItemType.Project  | true
        "Diff types"            | "A"    | 1    | QueryItemType.Project | "A"  | 1    | QueryItemType.Compound | false
        "Diff ids, diff names"  | "A"    | 1    | QueryItemType.Project | "B"  | 2    | QueryItemType.Project  | false
        "Diff names, diff types"| "A"    | 1    | QueryItemType.Project | "B"  | 1    | QueryItemType.Compound | false
        "Diff ids, diff types"  | "A"    | 1    | QueryItemType.Project | "A"  | 2    | QueryItemType.Compound | false

    }

    void "test truncating and adding ellipses when the name is too long"() {
        given:
        mockForConstraintsTests(QueryItem)
        String name = RandomStringUtils.randomAlphabetic(stringLength)
        String truncatedName = StringUtils.abbreviate(name, CartCompound.MAXIMUM_NAME_FIELD_LENGTH)

        QueryItem queryItem = new QueryItem(name, externalId, externalId, QueryItemType.Project)

        when:
        queryItem.validate()

        then:
        assert queryItem.name == truncatedName

        where:
        externalId | stringLength
        47         | 4001
        47         | 80000
        47         | 25
        2          | 0
    }

    void "test constraints on QueryItem #label"() {
        setup:
        mockForConstraintsTests(QueryItem)

        when:
        QueryItem queryItem = new QueryItem(name, id, id, type)
        queryItem.validate()

        then:
        queryItem.hasErrors() == !valid

        where:
        label              | name   | id   | type                  | valid
        "All null"         | null   | null | null                  | false
        "all populated"    | "A"    | 1    | QueryItemType.Project | true
        "null name"        | null   | 1    | QueryItemType.Project | false
        "null id"          | "A"    | null | QueryItemType.Project | false
        "null type"        | "A"    | 1    | null                  | false
        "negative id"      | "A"    | -1   | QueryItemType.Project | false
        "blank name"       | ""     | 1    | QueryItemType.Project | false
    }

}
