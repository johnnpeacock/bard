package bard.db.experiment

class Substance {
    Long id
	String smiles
	Date dateCreated
	Date lastUpdated
	String modifiedBy

	static mapping = {
		id( column: 'SUBSTANCE_ID', generator: 'assigned' )
	}

	static constraints = {
		smiles nullable: true, maxSize: 4000
		dateCreated(nullable: false)
		lastUpdated nullable: true
		modifiedBy nullable: true, maxSize: 40
	}
}
