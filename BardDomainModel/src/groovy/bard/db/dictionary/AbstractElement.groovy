package bard.db.dictionary

import bard.db.enums.ReadyForExtraction

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 8/22/12
 * Time: 1:46 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class AbstractElement {

    private static final int MODIFIED_BY_MAX_SIZE = 40
    private static final int LABEL_MAX_SIZE = 128
    private static final int DESCRIPTION_MAX_SIZE = 1000
    private static final int ABBREVIATION_MAX_SIZE = 20
    private static final int SYNONYMS_MAX_SIZE = 1000
    private static final int UNIT_MAX_SIZE = 128
    private static final int EXTERNAL_URL_MAX_SIZE = 1000
    private static final int READY_FOR_EXTRACTION_MAX_SIZE = 20
    private static final int ELEMENT_STATUS_MAX_SIZE = 20


    ElementStatus elementStatus = ElementStatus.Pending
    String label
    String description
    String abbreviation
    String synonyms
    String unit
    String externalURL
    ReadyForExtraction readyForExtraction = ReadyForExtraction.Pending

    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String modifiedBy

    static hasMany = [treeRoots: TreeRoot,
            ontologyItems: OntologyItem,
            childElementRelationships: ElementHierarchy,
            parentElementRelationships: ElementHierarchy]

    static mappedBy = [childElementRelationships: "childElement",
            parentElementRelationships: "parentElement"]

    static mapping = {
        id(column: 'ELEMENT_ID', generator: 'sequence', params: [sequence: 'ELEMENT_ID_SEQ'])
        unit(column: 'unit')
        externalURL(column: 'external_url')
    }

    static constraints = {
        elementStatus(nullable: false,maxSize: ELEMENT_STATUS_MAX_SIZE)

        label(nullable: false, unique: true, maxSize: LABEL_MAX_SIZE)
        description(nullable: true, maxSize: DESCRIPTION_MAX_SIZE)
        abbreviation(nullable: true, maxSize: ABBREVIATION_MAX_SIZE)
        synonyms(nullable: true, maxSize: SYNONYMS_MAX_SIZE)
        unit(nullable: true, maxSize: UNIT_MAX_SIZE)
        externalURL(nullable: true, maxSize: EXTERNAL_URL_MAX_SIZE)

        // TODO make enum
        readyForExtraction(nullable: false, maxSize: READY_FOR_EXTRACTION_MAX_SIZE)

        dateCreated(nullable: false)
        lastUpdated(nullable: true)
        modifiedBy(nullable: true, blank: false, maxSize: MODIFIED_BY_MAX_SIZE)
    }
}
enum ElementStatus {
    Pending,
    Published,
    Deprecated,
    Retired
}
