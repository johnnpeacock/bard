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

package bard.dm.cars.domainspreadsheetmapping

import bard.dm.Log
import bard.db.dictionary.Element
import bard.db.project.ProjectContextItem
import bard.db.project.ProjectContext

/**
 * Created with IntelliJ IDEA.
 * User: dlahr
 * Date: 9/3/12
 * Time: 12:56 AM
 * To change this template use File | Settings | File Templates.
 */
class ProjectAnnotater {
    private CarsBardMapping carsBardMapping

    private String username

    private static final String projectContextName = "project management"

    /**
     * @param carsBardMapping
     * @param username for use when creating domain objects - goes in "modified by" field
     */
    ProjectAnnotater(CarsBardMapping carsBardMapping, String username) {
        this.carsBardMapping = carsBardMapping
        this.username = username
    }

    void addAnnotations(Collection<ProjectPair> projectPairColl) {
        Log.logger.info("add annotations to projects")

        for (ProjectPair projectPair : projectPairColl) {
            Log.logger.info("\tproject uid: " + projectPair.carsProject.projectUid + " project id: " + projectPair.project.id)

            Set<Element> carsAttributeSet = new HashSet<Element>(carsBardMapping.projectAttributePropertyNameMap.keySet())

            for (ProjectContext projectContext : projectPair.project.contexts) {
                for (ProjectContextItem projectContextItem : projectContext.contextItems) {
                    if (carsAttributeSet.remove(projectContextItem.attributeElement)) {
                        Log.logger.info("\t\tannotation already present for " + projectContextItem.attributeElement.id + " " + projectContextItem.attributeElement.label)
                    }
                }
            }

            if (carsAttributeSet.size() > 0) {
                ProjectContext projectContext = new ProjectContext(project: projectPair.project, dateCreated: new Date(),
                        modifiedBy: username, contextName: projectContextName)
                projectPair.project.contexts.add(projectContext)
                assert projectContext.save()

                for (Element attribute : carsAttributeSet) {
                    Log.logger.info("\t\tadding annotation for " + attribute.id + " " + attribute.label)

                    ProjectContextItem projectContextItem = new ProjectContextItem(context: projectContext,
                            attributeElement: attribute, dateCreated: (new Date()), modifiedBy: username)
                    projectContext.contextItems.add(projectContextItem)


                    String carsProperty = carsBardMapping.projectAttributePropertyNameMap.get(attribute)
                    String carsValue = projectPair.carsProject.getProperty(carsProperty)

                    Map<String, Element> valueElementMap = carsBardMapping.projectElementValueElementMap.get(attribute)
                    if (valueElementMap != null) {
                        Log.logger.info("\t\t\tannotation is mapped")

                        Element valueElement = valueElementMap.get(carsValue.toLowerCase())

                        if (valueElement) {
                            projectContextItem.valueElement = valueElement
                            projectContextItem.valueDisplay = valueElement.label
                        } else {
                            Log.logger.info("\t\t\t\tcars to bard ontology mapping expected but not found - attribute: " + attribute.id + " cars value: " + carsValue)
                        }
                    } else {
                        Log.logger.info("\t\t\tannotation is not mapped, takes value directly")

                        projectContextItem.valueDisplay = carsValue
                    }

                    assert projectContextItem.save()
                }
            }
        }
    }
}
