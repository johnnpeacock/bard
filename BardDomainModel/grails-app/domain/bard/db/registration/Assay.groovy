package bard.db.registration

import bard.db.dictionary.Descriptor
import bard.db.enums.ReadyForExtraction
import bard.db.experiment.Experiment

class Assay {

    private static final int ASSAY_STATUS_MAX_SIZE = 20
    private static final int ASSAY_NAME_MAX_SIZE = 1000
    private static final int ASSAY_VERSION_MAX_SIZE = 10
    private static final int DESIGNED_BY_MAX_SIZE = 100
    private static final int READY_FOR_EXTRACTION_MAX_SIZE = 20
    private static final int MODIFIED_BY_MAX_SIZE = 40
    private static final int ASSAY_SHORT_NAME_MAX_SIZE = 250

    AssayStatus assayStatus = AssayStatus.Pending
    String assayShortName
    String assayName
    String assayVersion
    String designedBy
    ReadyForExtraction readyForExtraction = ReadyForExtraction.Pending
    String assayType = 'Regular'

    String modifiedBy
    // grails auto-timestamp
    Date dateCreated
    Date lastUpdated

    Set<Experiment> experiments = [] as Set<Experiment>
    Set<Measure> measures = [] as Set<Measure>
    List<AssayContext> assayContexts = [] as List<AssayContext>
    Set<AssayDocument> assayDocuments = [] as Set<AssayDocument>

    static hasMany = [experiments: Experiment,
            measures: Measure,
            assayContexts: AssayContext,
            assayDocuments: AssayDocument]

    static constraints = {
        assayStatus(maxSize: ASSAY_STATUS_MAX_SIZE, blank: false)
        assayShortName(maxSize: ASSAY_SHORT_NAME_MAX_SIZE, blank: false)
        assayName(maxSize: ASSAY_NAME_MAX_SIZE, blank: false)
        assayVersion(maxSize: ASSAY_VERSION_MAX_SIZE, blank: false)
        designedBy(nullable: true, maxSize: DESIGNED_BY_MAX_SIZE)
        readyForExtraction(maxSize: READY_FOR_EXTRACTION_MAX_SIZE, nullable: false)
        // TODO we can use enum mapping for this http://stackoverflow.com/questions/3748760/grails-enum-mapping
        // the ' - ' is this issue in this case
        assayType(inList: ['Regular', 'Panel - Array', 'Panel - Group', 'Template'])

        dateCreated(nullable: false)
        lastUpdated(nullable: true)
        modifiedBy(nullable: true, blank: false, maxSize: MODIFIED_BY_MAX_SIZE)
    }

    static mapping = {
        id(column: "ASSAY_ID", generator: "sequence", params: [sequence: 'ASSAY_ID_SEQ'])
        assayContexts(indexColumn: [name: 'DISPLAY_ORDER'], lazy: 'false')
    }

    static transients = ['assayContextItems']

    List<AssayContextItem> getAssayContextItems() {
        Set<AssayContextItem> assayContextItems = new HashSet<AssayContextItem>()
        for (AssayContext assayContext : this.assayContexts) {
            assayContextItems.addAll(assayContext.assayContextItems)
        }
        return assayContextItems as List<AssayContextItem>
    }

    /**
     * Create a map where all the assayContexts are grouped a common root in the ontology hierarchy based on a prefered
     * descriptor for the context.
     *
     * @return a Map keyed by the first 2 levels of the ontology hierarchy path with a each key having a list of assayContexts
     */
    Map<String, AssayContext> groupContexts() {


        Map<String, List<AssayContext>> mapByPath = this.assayContexts.groupBy { AssayContext assayContext ->
            String mapKey = 'uncategorized cards'
            Descriptor descriptor = assayContext.preferredDescriptor
            if (descriptor) {
                mapKey = descriptor.generateOntologyBreadCrumb(2)
            }
            mapKey
        }
        mapByPath as TreeMap<String, List<AssayContext>>
    }

    /**
     * duck typing to look like project
     * @return assayDocuments
     */
    Set<AssayDocument> getDocuments() {
        this.assayDocuments
    }
    /**
     *  duck typing to look like project
     * @return assayName
     */
    String getName() {
        this.assayName
    }

    String getDescription(){
        this.assayName
    }

}