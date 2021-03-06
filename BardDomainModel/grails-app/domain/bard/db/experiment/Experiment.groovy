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

package bard.db.experiment

import bard.db.enums.DocumentType
import bard.db.enums.ReadyForExtraction
import bard.db.enums.Status
import bard.db.enums.hibernate.ReadyForExtractionEnumUserType
import bard.db.enums.hibernate.StatusEnumUserType
import bard.db.model.AbstractContext
import bard.db.model.AbstractContextOwner
import bard.db.people.Person
import bard.db.people.Role
import bard.db.project.ProjectSingleExperiment
import bard.db.registration.Assay
import bard.db.registration.ExternalReference
import bard.db.registration.MeasureCaseInsensitiveDisplayLabelComparator
import org.apache.commons.lang3.StringUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import java.text.DateFormat
import java.text.SimpleDateFormat

class Experiment extends AbstractContextOwner {
    static final DateFormat dateFormat = new SimpleDateFormat("mm/dd/yyyy", Locale.US)
    public static final int EXPERIMENT_NAME_MAX_SIZE = 1000
    private static final int MODIFIED_BY_MAX_SIZE = 40
    private static final int APPROVED_BY_MAX_SIZE = 40
    public static final int DESCRIPTION_MAX_SIZE = 1000
    def capPermissionService
    def springSecurityService
    String experimentName
    Status experimentStatus = Status.DRAFT
    ReadyForExtraction readyForExtraction = ReadyForExtraction.NOT_READY
    Assay assay

    Date runDateFrom
    Date runDateTo
    Date holdUntilDate

    String description

    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String modifiedBy
    Long id;
    Long ncgcWarehouseId;
    Integer confidenceLevel = 1
    PanelExperiment panel;
    Person approvedBy
    Date approvedDate

    List<ExperimentContext> experimentContexts = []
    Set<ProjectSingleExperiment> projectExperiments = [] as Set
    Set<ExternalReference> externalReferences = [] as Set
    Set<ExperimentMeasure> experimentMeasures = [] as Set
    Set<ExperimentFile> experimentFiles = [] as Set
    Set<ExperimentDocument> experimentDocuments = [] as Set<ExperimentDocument>

    // if this is set, then don't automatically update readyForExtraction when this entity is dirty
    // this is needed to change the value to anything except "Ready"
    boolean disableUpdateReadyForExtraction = false


    static transients = ['measuresHaveAtLeastOnePriorityElement', 'experimentContextItems', 'disableUpdateReadyForExtraction']


    Role ownerRole //The team that owns this object. This is used by the ACL to allow edits etc

    List<ExperimentContextItem> getExperimentContextItems() {
        Set<ExperimentContextItem> experimentContextItems = new HashSet<ExperimentContextItem>()
        for (ExperimentContext experimentContext : this.experimentContexts) {
            experimentContextItems.addAll(experimentContext.experimentContextItems)
        }
        return experimentContextItems as List<ExperimentContextItem>
    }

    static belongsTo = [ownerRole: Role]
    static hasMany = [experimentContexts: ExperimentContext,
            experimentMeasures: ExperimentMeasure,
            externalReferences: ExternalReference,
            projectExperiments: ProjectSingleExperiment,
            experimentFiles: ExperimentFile,
            experimentDocuments: ExperimentDocument]

    static mapping = {
        id(column: "EXPERIMENT_ID", generator: "sequence", params: [sequence: 'EXPERIMENT_ID_SEQ'])
        experimentContexts(indexColumn: [name: 'DISPLAY_ORDER'], lazy: 'false')
        readyForExtraction(type: ReadyForExtractionEnumUserType)
        panel(column: 'PANEL_EXPRMT_ID')
        experimentStatus(type: StatusEnumUserType)
        approvedBy(column: "APPROVED_BY")
    }

    static constraints = {
        experimentName(nullable: false, blank: false, maxSize: EXPERIMENT_NAME_MAX_SIZE)
        experimentStatus(nullable: false)
        readyForExtraction(nullable: false)
        assay()
        panel(nullable: true)
        runDateFrom(nullable: true)
        runDateTo(nullable: true)
        holdUntilDate(nullable: true)

        description(nullable: true, blank: false, maxSize: DESCRIPTION_MAX_SIZE)
        confidenceLevel(nullable: true)
        dateCreated(nullable: false)
        lastUpdated(nullable: false)
        modifiedBy(nullable: true, blank: false, maxSize: MODIFIED_BY_MAX_SIZE)

        ncgcWarehouseId(nullable: true)
        ownerRole(nullable: false)
        approvedBy(nullable: true)
        approvedDate(nullable: true)
    }

    String getDisplayName() {
        return id + "-" + experimentName
    }

    @Override
    List getContexts() {
        return experimentContexts;
    }

    @Override
    void removeContext(AbstractContext context) {
        removeFromExperimentContexts(context)
    }

    @Override
    AbstractContext createContext(Map properties) {
        ExperimentContext context = new ExperimentContext(properties)
        addToExperimentContexts(context)
        return context
    }

    def afterInsert() {
        Experiment.withNewSession {
            capPermissionService?.addPermission(this)
        }
    }

    boolean measuresHaveAtLeastOnePriorityElement() {
        if (!experimentMeasures) {
            return false
        }
        for (ExperimentMeasure experimentMeasure : experimentMeasures) {
            if (experimentMeasure.priorityElement) {
                return true
            }
        }
        return false
    }

    String getOwner() {
        final String objectOwner = this.ownerRole?.displayName
        return objectOwner
    }

    public boolean permittedToSeeEntity() {
        if ((experimentStatus == Status.DRAFT) &&
                (!SpringSecurityUtils.ifAnyGranted('ROLE_BARD_ADMINISTRATOR') &&
                        !SpringSecurityUtils.principalAuthorities.contains(this.ownerRole))) {
            return false
        }
        return true
    }

    List<ExperimentDocument> getPublications() {
        final List<ExperimentDocument> documents = experimentDocuments.findAll {
            it.documentType == DocumentType.DOCUMENT_TYPE_PUBLICATION
        } as List<ExperimentDocument>
        documents.sort { p1, p2 -> p1.id.compareTo(p2.id) }
        return documents
    }

    List<ExperimentDocument> getExternalURLs() {
        final List<ExperimentDocument> documents = experimentDocuments.findAll {
            it.documentType == DocumentType.DOCUMENT_TYPE_EXTERNAL_URL
        } as List<ExperimentDocument>
        documents.sort { p1, p2 -> p1.id.compareTo(p2.id) }
        return documents
    }

    List<ExperimentDocument> getComments() {
        final List<ExperimentDocument> documents = experimentDocuments.findAll {
            it.documentType == DocumentType.DOCUMENT_TYPE_COMMENTS
        } as List<ExperimentDocument>
        documents.sort { p1, p2 -> p1.id.compareTo(p2.id) }
        return documents
    }

    List<ExperimentDocument> getDescriptions() {
        final List<ExperimentDocument> documents = experimentDocuments.findAll {
            it.documentType == DocumentType.DOCUMENT_TYPE_DESCRIPTION
        } as List<ExperimentDocument>
        documents.sort { p1, p2 -> p1.id.compareTo(p2.id) }
        return documents
    }

    List<ExperimentDocument> getProtocols() {
        final List<ExperimentDocument> documents = experimentDocuments.findAll {
            it.documentType == DocumentType.DOCUMENT_TYPE_PROTOCOL
        } as List<ExperimentDocument>
        documents.sort { p1, p2 -> p1.id.compareTo(p2.id) }
        return documents
    }

    List<ExperimentDocument> getOtherDocuments() {
        final List<ExperimentDocument> documents = experimentDocuments.findAll {
            it.documentType == DocumentType.DOCUMENT_TYPE_OTHER
        } as List<ExperimentDocument>
        documents.sort { p1, p2 -> p1.id.compareTo(p2.id) }
        return documents
    }

    Set<ExperimentDocument> getDocuments() {
        this.experimentDocuments
    }

    Collection<ExperimentMeasure> getRootMeasures() {
        return experimentMeasures.findAll { it.parent == null }
    }
    /**
     * @return a list of Measures without parents sorted by displayLabel case insensitive
     */
    List<ExperimentMeasure> getRootMeasuresSorted() {
        return experimentMeasures.findAll { it.parent == null }.sort(new MeasureCaseInsensitiveDisplayLabelComparator())
    }


}
