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

package bard.db.dictionary

import bard.db.enums.ReadyForExtraction
import bard.db.enums.hibernate.ReadyForExtractionEnumUserType

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 8/22/12
 * Time: 1:46 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class AbstractElement {

    public static final int LABEL_MAX_SIZE = 128
    public static final int DESCRIPTION_MAX_SIZE = 1000
    public static final int ABBREVIATION_MAX_SIZE = 30
    public static final int SYNONYMS_MAX_SIZE = 1000
    public static final int BARD_URI_MAX_SIZE = 250
    private static final int EXTERNAL_URL_MAX_SIZE = 1000
    private static final int MODIFIED_BY_MAX_SIZE = 40


    ElementStatus elementStatus = ElementStatus.Pending
    String label
    String description
    String abbreviation
    String synonyms
    Element unit
    String bardURI
    String externalURL
    ReadyForExtraction readyForExtraction = ReadyForExtraction.NOT_READY
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String modifiedBy



    static constraints = {
        elementStatus(nullable: false)

        label(nullable: false, unique: true, maxSize: LABEL_MAX_SIZE)
        unit(nullable: true)
        abbreviation(nullable: true, maxSize: ABBREVIATION_MAX_SIZE)
        bardURI(nullable: true, maxSize: BARD_URI_MAX_SIZE)
        description(nullable: true, maxSize: DESCRIPTION_MAX_SIZE)
        synonyms(nullable: true, maxSize: SYNONYMS_MAX_SIZE)
        externalURL(nullable: true, maxSize: EXTERNAL_URL_MAX_SIZE)

        readyForExtraction(nullable: false)

        dateCreated(nullable: false)
        lastUpdated(nullable: true)
        modifiedBy(nullable: true, blank: false, maxSize: MODIFIED_BY_MAX_SIZE)
    }
    static mapping = {
        id(column: 'ELEMENT_ID', generator: 'sequence', params: [sequence: 'ELEMENT_ID_SEQ'])
        bardURI(column: 'BARD_URI')
        externalURL(column: 'EXTERNAL_URL')
        readyForExtraction(type: ReadyForExtractionEnumUserType)
    }
}

