package bard.db.people

class Person {
    public static final int NAME_MAX_SIZE = 255
    public static final int MODIFIED_BY_MAX_SIZE = 40

    String userName
    String emailAddress
    String fullName
    boolean accountExpired
    boolean accountLocked
    boolean accountEnabled
    Date dateCreated = new Date()
    Date lastUpdated
    String modifiedBy
    Role newObjectRole

    static belongsTo = [newObjectRole: Role]

    static mapping = {
        table('PERSON')
        id(column: 'PERSON_ID', generator: "sequence", params: [sequence: 'PERSON_ID_SEQ'])
        version(false)
        userName(column: 'USERNAME')
        newObjectRole(column: 'NEW_OBJECT_ROLE')
    }
    static transients = ["rolesAsList"]

    static constraints = {
        userName(blank: false, maxSize: NAME_MAX_SIZE)
        emailAddress(nullable: true, maxSize: NAME_MAX_SIZE)
        fullName(nullable: true, maxSize: NAME_MAX_SIZE)
        dateCreated(nullable: false)
        lastUpdated(nullable: true)
        modifiedBy(nullable: true, blank: false, maxSize: MODIFIED_BY_MAX_SIZE)
        newObjectRole(nullable: true)
    }

    Set<Role> getRoles() {

        PersonRole.withNewSession {  //see http://jira.grails.org/browse/GRAILS-8450
            return PersonRole.findAllByPerson(this).collect { it.role } as Set
        }
    }

    boolean isAdmin() {
        final Set<Role> foundRoles = getRoles()
        boolean  foundAdmin = false
        foundRoles.collect {
            if (it.authority == "ROLE_BARD_ADMINISTRATOR") {
                foundAdmin = true
                return
            }
        }
        return foundAdmin
    }

    String getRolesAsList() {
        List<String> displayNames = []
        for (Role role : getRoles()) {
            displayNames.add(role.displayName)
        }
        return displayNames.join(",")
    }
}