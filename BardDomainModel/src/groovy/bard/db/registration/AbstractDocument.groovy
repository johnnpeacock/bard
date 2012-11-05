package bard.db.registration

/**
 * Created with IntelliJ IDEA.
 * User: ddurkin
 * Date: 11/5/12
 * Time: 4:18 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class AbstractDocument {

    private static final int DOCUMENT_NAME_MAX_SIZE = 500
    private static final int DOCUMENT_TYPE_MAX_SIZE = 20
    private static final int MODIFIED_BY_MAX_SIZE = 40

    String documentName
    String documentType
    String documentContent
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    static constraints = {
        documentName(blank: false, maxSize: DOCUMENT_NAME_MAX_SIZE)
        documentType(blank: false, maxSize: DOCUMENT_TYPE_MAX_SIZE, inList: ['Description', 'Protocol', 'Comments', 'Paper', 'External URL', 'Other'])
        documentContent(nullable: true, blank: false)
        dateCreated(nullable: false)
        lastUpdated(nullable: true)
        modifiedBy(nullable: true, blank: false, maxSize: MODIFIED_BY_MAX_SIZE)
    }
}
