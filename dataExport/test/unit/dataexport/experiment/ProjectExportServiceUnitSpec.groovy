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

package dataexport.experiment

import bard.db.dictionary.Element
import bard.db.enums.ContextType
import bard.db.enums.DocumentType
import bard.db.enums.ReadyForExtraction
import bard.db.experiment.PanelExperiment
import bard.db.project.*
import bard.db.registration.ExternalReference
import common.tests.XmlTestAssertions
import dataexport.registration.MediaTypesDTO
import exceptions.NotFoundException
import grails.buildtestdata.TestDataConfigurationHolder
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Unroll

import static bard.db.enums.ReadyForExtraction.*
import static common.tests.XmlTestSamples.*

/**
 * Created with IntelliJ IDEA.
 * User: jasiedu
 * Date: 6/19/12
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
@Build([ExternalReference, Project, ProjectContext, ProjectContextItem, ProjectDocument, ProjectSingleExperiment,
 ProjectExperimentContext, ProjectExperimentContextItem, StepContext, StepContextItem, ProjectPanelExperiment, PanelExperiment])
@Mock([ExternalReference, Project, ProjectContext, ProjectContextItem, ProjectDocument, ProjectSingleExperiment,
 ProjectExperimentContext, ProjectExperimentContextItem, StepContext, StepContextItem])
@Unroll
class ProjectExportServiceUnitSpec extends Specification {
    Writer writer
    MarkupBuilder markupBuilder

    ProjectExportService projectExportService

    Resource projectSchema = new FileSystemResource(new File("web-app/schemas/projectSchema.xsd"))

    void setup() {
        LinkGenerator grailsLinkGenerator = Mock(LinkGenerator.class)

        MediaTypesDTO mediaTypesDTO =
            new MediaTypesDTO(projectMediaType: "application/vnd.bard.cap+xml;type=project",
                    projectsMediaType: "application/vnd.bard.cap+xml;type=projects",
                    externalReferenceMediaType: "application/vnd.bard.cap+xml;type=externalReference",
                    elementMediaType: "application/vnd.bard.cap+xml;type=element",
                    experimentMediaType: "application/vnd.bard.cap+xml;type=experiment",
                    projectDocMediaType: "application/vnd.bard.cap+xml;type=projectDoc")

        this.projectExportService = new ProjectExportService()
        projectExportService.grailsLinkGenerator = grailsLinkGenerator
        projectExportService.mediaTypesDTO = mediaTypesDTO
        this.writer = new StringWriter()
        this.markupBuilder = new MarkupBuilder(writer)
        TestDataConfigurationHolder.reset()
    }

    void "test Generate Projects #label"() {
        given:
        for (ReadyForExtraction rfe in projectReadyForExtractionList) {
            Project.build(readyForExtraction: rfe)
        }

        when:
        this.projectExportService.generateProjects(this.markupBuilder)


        then:
        String actualXml = this.writer.toString()
        XmlTestAssertions.assertResults(results, actualXml)
        XmlTestAssertions.validate(projectSchema, actualXml)

        where:
        label                    | projectReadyForExtractionList         | results
        "no projects"            | []                                    | PROJECTS_NO_PROJECTS_READY
        "one project"            | [READY]                               | PROJECTS_ONE_PROJEC_READY
        "only one project Ready" | [NOT_READY, READY, STARTED, COMPLETE] | PROJECTS_ONE_PROJEC_READY
    }

    void "test Generate Project Not Found Exception"() {
        when: "We attempt to generate a Project"
        this.projectExportService.generateProject(this.markupBuilder, new Long("2"))

        then: "An exception should be thrown"
        thrown(NotFoundException)
    }

    void "generate ProjectContext #label"() {
        given:
        ProjectContext context = ProjectContext.build(map)
        numItems.times { ProjectContextItem.build(context: context) }

        when:
        this.projectExportService.generateProjectContext(this.markupBuilder, context)

        then:
        String actualXml = this.writer.toString()
        XmlTestAssertions.assertResults(results, actualXml)
        XmlTestAssertions.validate(projectSchema, actualXml)

        where:
        label                         | results                        | numItems | map
        "Minimal"                     | CONTEXT_MINIMAL                | 0        | [contextType: ContextType.UNCLASSIFIED]
        "Minimal with name"           | CONTEXT_MINIMAL_WITH_NAME      | 0        | [contextType: ContextType.UNCLASSIFIED, contextName: 'contextName']
        "Minimal with group"          | CONTEXT_MINIMAL_WITH_GROUP     | 0        | [contextType: ContextType.BIOLOGY]
        "Minimal with 1 contextItem"  | CONTEXT_MINIMAL_WITH_ONE_ITEM  | 1        | [contextType: ContextType.UNCLASSIFIED]
        "Minimal with 2 contextItems" | CONTEXT_MINIMAL_WITH_TWO_ITEMS | 2        | [contextType: ContextType.UNCLASSIFIED]
    }

    void "generate ProjectExperiment #label"() {
        given:
        ProjectExperiment projectExperiment = ProjectSingleExperiment.build(mapClosure.call())
        numContext.times { ProjectExperimentContext.build(projectExperiment: projectExperiment, contextType: ContextType.UNCLASSIFIED) }

        when:
        this.projectExportService.generateProjectExperiment(this.markupBuilder, projectExperiment)
        numContext

        then:
        String actualXml = this.writer.toString()
        XmlTestAssertions.assertResults(results, actualXml)
        XmlTestAssertions.validate(projectSchema, actualXml)

        where:
        label              | results                          | numContext | mapClosure
        "Minimal"          | PROJECT_EXPERIMENT_MINIMAL       | 0          | { [:] }
        "With stageRef"    | PROJECT_EXPERIMENT_WITH_STAGEREF | 0          | { [stage: Element.build(label: 'stage')] }

    }


    void "generate ProjectStep #label"() {
        given:
        ProjectStep projectStep = ProjectStep.build(map)
        numContext.times { StepContext.build(projectStep: projectStep, contextType: ContextType.UNCLASSIFIED) }

        when:
        this.projectExportService.generateProjectStep(this.markupBuilder, projectStep)

        then:
        String actualXml = this.writer.toString()
        XmlTestAssertions.assertResults(results, actualXml)
        XmlTestAssertions.validate(projectSchema, actualXml)

        where:
        label              | results                     | numContext | map
        "Minimal"          | PROJECT_STEP_MINIMAL        | 0          | [:]
        "With edgeName"    | PROJECT_STEP_WITH_EDGE_NAME | 0          | [edgeName: 'edge']
    }

    void "generate StepContext #label"() {
        given:
        StepContext stepContext = StepContext.build(map)
        numItems.times { StepContextItem.build(stepContext: stepContext) }

        when:
        this.projectExportService.generateStepContext(this.markupBuilder, stepContext)

        then:
        String actualXml = this.writer.toString()
        XmlTestAssertions.assertResults(results, actualXml)
        XmlTestAssertions.validate(projectSchema, actualXml)

        where:
        label                  | results                        | numItems | map
        "Minimal"              | CONTEXT_MINIMAL                | 0        | [contextType: ContextType.UNCLASSIFIED]
        "with contextName"     | CONTEXT_MINIMAL_WITH_NAME      | 0        | [contextType: ContextType.UNCLASSIFIED, contextName: 'contextName']
        "with contextGroup"    | CONTEXT_MINIMAL_WITH_GROUP     | 0        | [contextType: ContextType.BIOLOGY]
        "context with 1 item"  | CONTEXT_MINIMAL_WITH_ONE_ITEM  | 1        | [contextType: ContextType.UNCLASSIFIED]
        "context with 2 items" | CONTEXT_MINIMAL_WITH_TWO_ITEMS | 2        | [contextType: ContextType.UNCLASSIFIED]
    }

    void "generate ProjectDocument #label"() {
        given:
        ProjectDocument document = ProjectDocument.build(map)

        when:
        this.projectExportService.generateProjectDocument(this.markupBuilder, document)

        then:
        String actualXml = this.writer.toString()
        XmlTestAssertions.assertResults(results, actualXml)
        XmlTestAssertions.validate(projectSchema, actualXml)

        where:
        label          | results                       | map
        "Minimal"      | PROJECT_DOCUMENT_MINIMAL      | [documentType: DocumentType.DOCUMENT_TYPE_DESCRIPTION]
        "with content" | PROJECT_DOCUMENT_WITH_CONTENT | [documentContent: 'documentContent', documentType: DocumentType.DOCUMENT_TYPE_DESCRIPTION]

    }

    void "test generate Project with a panel"() {
        given:
        Project project = Project.build(readyForExtraction: READY)
        PanelExperiment panelExperiment = PanelExperiment.build()
        ProjectPanelExperiment pe = ProjectPanelExperiment.build(project: project, panelExperiment: panelExperiment);

        when: "We attempt to generate a Project XML document"
        this.projectExportService.generateProject(this.markupBuilder, project)

        then: "A valid xml document is generated and is similar to the expected document"
        String actualXml = this.writer.toString()
        XmlTestAssertions.assertResultsWithOverrideAttributes(PROJECT_MINIMAL, actualXml)
        XmlTestAssertions.validate(projectSchema, actualXml)
    }

    void "test generate Project #label"() {
        given: "A Project"
        map << [readyForExtraction: READY]   // in this test always setting readyForExtraction to Ready
        final Project project = Project.build(map)
        numExtRef.times { ExternalReference.build(project: project) }
        numDoc.times { ProjectDocument.build(project: project) }
        numPrjCtx.times { ProjectContext.build(project: project, contextType: ContextType.UNCLASSIFIED) }
        numPrjExp.times {
            ProjectSingleExperiment pe = ProjectSingleExperiment.build(project: project)
            pe.experiment.readyForExtraction = ReadyForExtraction.READY
            numPrjExpCtx.times { ProjectExperimentContext.build(projectExperiment: pe, contextType: ContextType.UNCLASSIFIED) }
        }
        numPrjStep.times { ProjectStep.build(previousProjectExperiment: project.projectExperiments.first(), nextProjectExperiment: project.projectExperiments.last()) }

        when: "We attempt to generate a Project XML document"
        this.projectExportService.generateProject(this.markupBuilder, project)

        then: "A valid xml document is generated and is similar to the expected document"
        String actualXml = this.writer.toString()
        XmlTestAssertions.assertResultsWithOverrideAttributes(results, actualXml)
        XmlTestAssertions.validate(projectSchema, actualXml)

        where:
        label                               | results                                       | numExtRef | numDoc | numPrjCtx | numPrjExp | numPrjExpCtx | numPrjStep | map
        "Minimal"                           | PROJECT_MINIMAL                               | 0         | 0      | 0         | 0         | 0            | 0          | [:]
        "With Description"                  | PROJECT_WITH_DESCRIPTION                      | 0         | 0      | 0         | 0         | 0            | 0          | [description: 'description']

        "with 1 ExternalReference"          | PROJECT_ONE_EXTERNAL_REFERENCE                | 1         | 0      | 0         | 0         | 0            | 0          | [:]
        "with 2 ExternalReferences"         | PROJECT_TWO_EXTERNAL_REFERENCES               | 2         | 0      | 0         | 0         | 0            | 0          | [:]

        "with 1 Document"                   | PROJECT_ONE_DOCUMENT                          | 0         | 1      | 0         | 0         | 0            | 0          | [:]
        "with 2 Documents"                  | PROJECT_TWO_DOCUMENTS                         | 0         | 2      | 0         | 0         | 0            | 0          | [:]

        "With 1 context"                    | PROJECT_WITH_ONE_CONTEXT                      | 0         | 0      | 1         | 0         | 0            | 0          | [:]
        "With 2 contexts"                   | PROJECT_WITH_TWO_CONTEXT                      | 0         | 0      | 2         | 0         | 0            | 0          | [:]

        "With 1 context and 1 experiment"   | PROJECT_WITH_ONE_CONTEXT_ONE_EXPERIMENT       | 0         | 0      | 1         | 1         | 0            | 0          | [:]

        "With 1 experiment"                 | PROJECT_WITH_EXPERIMENT                       | 0         | 0      | 0         | 1         | 0            | 0          | [:]

        "With 2 experiments 1 Project step" | PROJECT_WITH_TWO_EXPERIMENTS_ONE_PROJECT_STEP | 0         | 0      | 0         | 2         | 0            | 1          | [:]
    }

    void "test generate ProjectExperiment experiment - Not Ready #label"() {
        given: "A Project"
        map << [readyForExtraction: READY]   // in this test always setting readyForExtraction to Ready
        final Project project = Project.build(map)
        numExtRef.times { ExternalReference.build(project: project) }
        numDoc.times { ProjectDocument.build(project: project) }
        numPrjCtx.times { ProjectContext.build(project: project, contextType: ContextType.UNCLASSIFIED) }
        numPrjExp.times {
            ProjectSingleExperiment pe = ProjectSingleExperiment.build(project: project)
            pe.experiment.readyForExtraction = ReadyForExtraction.NOT_READY
            numPrjExpCtx.times { ProjectExperimentContext.build(projectExperiment: pe, contextType: ContextType.UNCLASSIFIED) }
        }
        numPrjStep.times { ProjectStep.build(previousProjectExperiment: project.projectExperiments.first(), nextProjectExperiment: project.projectExperiments.last()) }

        when: "We attempt to generate a Project XML document"
        this.projectExportService.generateProject(this.markupBuilder, project)

        then: "A valid xml document is generated and is similar to the expected document"
        String actualXml = this.writer.toString()
        XmlTestAssertions.assertResultsWithOverrideAttributes(results, actualXml)
        XmlTestAssertions.validate(projectSchema, actualXml)

        where:
        label               | results                           | numExtRef | numDoc | numPrjCtx | numPrjExp | numPrjExpCtx | numPrjStep | map
        "With 1 experiment" | PRJECT_WITH_NO_PROJECT_EXPERIMENT | 0         | 0      | 0         | 1         | 0            | 0          | [:]
    }

    void "test generated Project modifiedBy #label"() {
        given: "An Project"
        Project project = Project.build()
        project.modifiedBy = modifiedBy

        when: "We attempt to generate an project XML document"
        this.projectExportService.generateProject(this.markupBuilder, project)

        then:
        String actualXml = this.writer.toString()
        def resultXml = new XmlSlurper().parseText(actualXml)
        resultXml.@modifiedBy == expectedModifiedBy

        where:
        label           | modifiedBy    | expectedModifiedBy
        "without email" | 'foo'         | 'foo'
        "with email"    | 'foo@foo.com' | 'foo'

    }


}
