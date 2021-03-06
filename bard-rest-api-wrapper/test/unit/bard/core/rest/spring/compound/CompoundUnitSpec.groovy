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

package bard.core.rest.spring.compound

import bard.core.rest.spring.compounds.Compound
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class CompoundUnitSpec extends Specification {
    @Shared
    ObjectMapper objectMapper = new ObjectMapper()

    public static final String COMPOUND = '''
        {
           "cid": "3899",
           "iso_smiles": "CC1=C(C=NO1)C(=O)NC2=CC=C(C=C2)C(F)(F)F",
           "iupac_name": "5-methyl-N-[4-(trifluoromethyl)phenyl]-1,2-oxazole-4-carboxamide",
           "preferred_term": "Leflunomide",
           "compound_class": "Drug",
           "highlight": null,
           "bardProjectId" : 1,
           "capProjectId" : 1
       }
       '''

    void "test serialization to Compound"() {
        when:
        final Compound compound = objectMapper.readValue(COMPOUND, Compound.class)
        compound.setEtag("etag")
        compound.setAnno_key([])
        compound.setAnno_val([])
        then:
        assert compound.getCid() == 3899

        assert compound.getSmiles() == "CC1=C(C=NO1)C(=O)NC2=CC=C(C=C2)C(F)(F)F"
        assert compound.getIupacName() == "5-methyl-N-[4-(trifluoromethyl)phenyl]-1,2-oxazole-4-carboxamide"
        assert compound.getName() == "Leflunomide"
        assert compound.getFreeTextName() =="Leflunomide"
        assert compound.getFreeTextCompoundClass() == compound.getCompoundClass()
        assert compound.getFreeTextIupacName() == compound.getIupacName()
        assert compound.getFreeTextName() == compound.name
        assert compound.getFreeTextSmiles() == compound.smiles
        assert compound.getCompoundClass() == "Drug"
        assert !compound.getHighlight()
        assert !compound.getComplexity()
        assert compound.getId()==3899
        assert compound.getEtag() == "etag"
        assert !compound.getResourcePath()
        assert !compound.getAnno_key()
        assert !compound.getAnno_val()
        assert compound.isDrug()
        assert !compound.isProbe()
        assert compound.bardProjectId == 1
        assert compound.capProjectId == 1
    }


}

