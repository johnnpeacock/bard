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

package maas

import bard.db.experiment.Experiment
import bard.db.experiment.ExperimentContextItem
import bard.db.dictionary.Element
import bard.db.registration.AttributeType
import bard.db.project.Project
import bard.db.project.ProjectExperiment
import bard.db.model.AbstractContextItem
import bard.db.project.ProjectContextItem
import org.apache.commons.lang3.StringUtils
import bard.db.registration.ExternalReference
import org.apache.commons.lang.StringUtils
import bard.db.model.AbstractContext
import org.apache.commons.lang3.StringUtils
import org.apache.commons.math3.util.Precision

class ContextHandlerService {
    private static final String goElementLabel = "GO biological process term"
    private static final String speciesElementLabel = "species name"
    static final Map<String, String> externalTermMap = ExternalTermMapping.build()


    AbstractContextItem updateContextItem(ContextItemDto contextItemDto, String loadedBy, List<String> errorMessages, String contextType) {
        if (!contextItemDto.value)
            return
        Element attributeElement = Element.findByLabelIlike(contextItemDto.key)
        if (!attributeElement) {
            final String message = "Attribute element not exist: (${contextItemDto.key})"
            errorMessages << message
        }
        AbstractContextItem contextItem = null
        if (contextType == "Experiment")
            contextItem = new ExperimentContextItem(attributeElement: attributeElement, modifiedBy: loadedBy)
        else if (contextType == "Project")
            contextItem = new ProjectContextItem(attributeElement: attributeElement, modifiedBy: loadedBy)

        Element concentrationUnitsElement = contextItemDto.concentrationUnits ? Element.findByLabelIlike(contextItemDto.concentrationUnits) : null
        String concentrationUnitsAbbreviation = concentrationUnitsElement ? " ${concentrationUnitsElement.abbreviation}" : ""
        //populate attribute-value type and value
        Element valueElement = contextItemDto.value ? Element.findByLabelIlike(contextItemDto.value) : null


        //if the value string could be matched against an element then add it to the valueElement
        if (valueElement) {
            contextItem.valueElement = valueElement
            contextItem.valueDisplay = valueElement.label
        }
        //else, if the attribute's value is a number value, store it in the valueNum field
        else if (contextItemDto.value && (!(contextItemDto.value instanceof String) || contextItemDto.value.isNumber())) {
            Float val = new Float(contextItemDto.value)
            contextItem.valueNum = val
            contextItem.qualifier = "= "
            //If the value is a number and also has concentration-units, we need to find the units element ID and update the valueDisplay accrdingly
            contextItem.valueDisplay = val.toString() + concentrationUnitsAbbreviation
        }
        //else, if the attribute is a numeric range (e.g., 440-460nm -> 440-460), then store it in valueMin, valueMax and make AttributeType=range.
        else if (contextItemDto.value && (contextItemDto.value instanceof String) && contextItemDto.value.matches(/^\d+\-\d+$/)) {
            final String[] rangeStringArray = contextItemDto.value.split("-")
            contextItem.valueMin = new Float(rangeStringArray[0])
            contextItem.valueMax = new Float(rangeStringArray[1])
            contextItem.valueDisplay = contextItemDto.value + concentrationUnitsAbbreviation //range-units are reported separately.
        }
        //else, if the attribute's is a type-in or attribute-type is Free, then simply store it the valueDisplay field
        else if (contextItemDto.typeIn || (contextItemDto.attributeType == AttributeType.Free)) {
            contextItem.valueDisplay = contextItemDto.value
            def nameElementId = [565l, //assay provider name
                    568l, // project lead name
                    1823l] // science officer
            if (nameElementId.contains(contextItem.attributeElement.id))
                contextItem.extValueId = contextItemDto.value      // put a value as a place holder in order to pass validation
        }
        else {
            final String message = "Can not handle Key: ${contextItemDto.key}, Value: ${contextItemDto.value}"
            errorMessages << message
        }

        //populate the qualifier field, if exists, and prefix the valueDisplay with it
        if (contextItemDto.qualifier) {
            contextItem.qualifier = String.format('%-2s', contextItemDto.qualifier)
            contextItem.valueDisplay = "${contextItemDto.qualifier}${contextItem.valueDisplay}"
        }

        /* clean up context item value */
        if (! postProcessContextItem(contextItem, errorMessages)) {
            println("Error during post process of context item " + contextItemDto)
        }
        return contextItem
    }


    /**
     * Find experiments by aid, we are expecting one experiment corresponding to one aid, if there are multiple, return the first one
     * @param aid
     * @return an experiment
     */
    Experiment getExperimentFromAid(long aid) {
        def criteria = Experiment.createCriteria()
        List<Experiment> results = criteria.list {
            externalReferences {
                eq('extAssayRef', "aid=${aid.toString()}")
            }
        }
        return (results && (results.size() == 1)) ? results.first() : null
    }

    /**
     * Find projects by aid, we are expecting one aid only corresponding to one project, (need to confirm if it is true)
     * if there are multiple, return the first one
     * @param aid
     * @return a project
     */
    Project getProjectFromAid(long aid) {
        def criteria = Project.createCriteria()
        List<Project> results = criteria.list {
            externalReferences {
                eq('extAssayRef', "aid=${aid.toString()}")
            }
        }
        return (results && (results.size() == 1)) ? results.first() : null
    }

    /**
     * Find projects by uid, we are expecting one uid only corresponding to one project,
     * if there are multiple, return the first one
     * @param uid
     * @return a project
     */
    Project getProjectFromProjectUID(long uid) {
        def criteria = Project.createCriteria()
        List<Project> results = criteria.list {
            externalReferences {
                eq('extAssayRef', "project_UID=${uid.toString()}")
            }
        }
        return (results && (results.size() == 1)) ? results.first() : null
    }

    /**
     *  if the value field is of the format 'cid:12345678' then:
     *  1. lookup in the Element table for the element.label='PubChem CID' and take the element.externalUrl value from it
     *  2. Strip out the 'cid:' part and use the numeric value to concatenate: externalUrl + cid_number (e.g., 'http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=12345678'
     *  3. Use that to populate the valueDisplay
     *  4. Use the element.id as the ValueElement.id
     *
     *  if the value field is of the format 'Uniprot:Q03164' do same as above but for 'UniProt' in the element table
     */
    private boolean postProcessContextItem(AbstractContextItem contextItem, List<String> errorMessages) {
        String display = contextItem.valueDisplay
        if (StringUtils.isBlank(display))
            return true

        if (display.toLowerCase().find(/^cid\W*:\W*\d+\W*/)) {//'cid:12345678'
            return rebuildAssayContextItem(contextItem, 'PubChem CID', errorMessages)
        } else if (display.toLowerCase().find(/^uniprot\W*:/)) {//'Uniprot:Q03164'
            return rebuildAssayContextItem(contextItem, 'UniProt accession number', errorMessages)
        } else if (display.toLowerCase().find(/^uniprotkb\W*:/)) {// 'UnitProtKB:UniProtKB:Q9QUQ5'
            return rebuildAssayContextItem(contextItem, 'UniProt accession number', errorMessages)
        }else if (display.toLowerCase().find(/^gi\W*:/)) {//'gi:10140845'
            return rebuildAssayContextItem(contextItem, 'GenBank ID', errorMessages)
        } else if (display.toLowerCase().indexOf("go:") >= 0) {//'go:
            return rebuildAssayContextItem(contextItem, goElementLabel, errorMessages)
        } else if (contextItem.attributeElement.label.equals(speciesElementLabel)) {//'go:
            return rebuildAssayContextItem(contextItem, speciesElementLabel, errorMessages)
        }
        return true
    }

    private boolean rebuildAssayContextItem(AbstractContextItem contextItem, String findByLabelIlike, List<String> errorMessages) {
        String extValueId = StringUtils.split(contextItem.valueDisplay, ':').toList().last().trim()
        if (findByLabelIlike.equals(goElementLabel) || findByLabelIlike.equals(speciesElementLabel)) {
            if (externalTermMap.containsKey(extValueId.toLowerCase())) {
                extValueId = externalTermMap.get(extValueId.toLowerCase())
            } else if (!StringUtils.isNumeric(extValueId)) {
                println("possible GO term or species name that is not mapped: ${extValueId}")
            }
        }

        Element element = Element.findByLabelIlike(findByLabelIlike)
        if (!element) {
            errorMessages << "element ${contextItem.valueDisplay} not found in database ${findByLabelIlike}"
            return false
        }

        String newValueDisplay = "${element.externalURL}${extValueId}"
        contextItem.attributeElement = element
        contextItem.extValueId = extValueId
        contextItem.valueDisplay = newValueDisplay

        return true
    }

    /**
     * Loop over the whole context to see if a particular item exist or not
     *
     * @param context
     * @param item
     * @return
     */
//    boolean isContextItemExist(AbstractContext context, AbstractContextItem item)  {
//        boolean isSame = false
//        context.contextItems.each{ AbstractContextItem it ->
//            if (item.attributeElement.label == it.attributeElement.label &&
//                    item.valueElement?.label == it.valueElement?.label &&
//                    StringUtils.equals(item.extValueId, it.extValueId) &&
//                    StringUtils.equals(item.qualifier, it.qualifier) &&
////                    Float.compare(item.valueNum, it.valueNum) &&
////                    Float.compare(item.valueMin, it.valueMin) &&
////                    Float.compare(item.valueMax, it.valueMax) &&
//                    StringUtils.equals(item.valueDisplay, it.valueDisplay)
//                )
//                isSame = true
//        }
//        return isSame
//    }

    /**
     * Loop over the whole context to see if a particular item exist or not
     *
     * @param context
     * @param item
     * @return
     */
    boolean isContextItemExist(AbstractContext context, AbstractContextItem item) {
        for (AbstractContextItem aci : context.contextItems) {
            if (isAbstractContextItemSame(aci, item))
                return true
        }
        return false
    }

    boolean isAbstractContextItemSame(AbstractContextItem a, AbstractContextItem b) {
        float eps = 0.00001
        if (    (a.attributeElement == b.attributeElement) &&
                (a.valueElement == b.valueElement) &&
                (a.extValueId == b.extValueId) &&
                (Precision.equalsIncludingNaN(nullToNaN(a.valueNum), nullToNaN(b.valueNum), eps) && StringUtils.equals(a.qualifier, b.qualifier)) &&
                (Precision.equalsIncludingNaN(nullToNaN(a.valueMin), nullToNaN(b.valueMin), eps) && Precision.equalsIncludingNaN(nullToNaN(a.valueMax), nullToNaN(b.valueMax), eps)) &&
                (StringUtils.equals(a.valueDisplay, b.valueDisplay))
        )
            return true
        return false
    }
    Float nullToNaN(Float a) {
        if (!a)
            return Float.NaN
        return a
    }

//    boolean isContextSame(AbstractContext a, AbstractContext b) {
//        for (AbstractContextItem aci : a.contextItems) {
//            if (!isContextItemExist(b, aci))
//                return false
//        }
//        return true
//    }

    int isContextSame(AbstractContext a, AbstractContext b) {
        int aInB = 0
        for (AbstractContextItem aci : a.contextItems) {
            if (isContextItemExist(b, aci)) {
                aInB++;
            }
        }
        if (aInB == a.contextItems.size() && aInB == b.contextItems.size())  {  // the same
            return 0
        }
        else if (aInB == a.contextItems.size() && aInB < b.contextItems.size()) {  // a < b
            return -1
        }
        else if (aInB == b.contextItems.size() && aInB < a.contextItems.size()) { // a > b
            return 1
        }
        return 999
    }
}
