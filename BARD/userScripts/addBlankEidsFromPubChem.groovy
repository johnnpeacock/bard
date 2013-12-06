import bard.db.experiment.Experiment
import bard.db.registration.ExternalReference
import bard.db.registration.Assay
import bard.db.people.Role
import bard.db.registration.ExternalSystem
import bard.pubchem.xml.PubChemXMLParserFactory
import bard.pubchem.model.PCAssay
import bard.pubchem.model.PCAssayXRef
import bard.pubchem.model.XRef
import bard.db.enums.DocumentType
import bard.db.experiment.ExperimentDocument
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import bard.db.project.ProjectSingleExperiment
import bard.db.project.Project


final String pubmedUrlPrefix = "http://www.ncbi.nlm.nih.gov/pubmed/"

SpringSecurityUtils.reauthenticate('integrationTestUser', null)

final Long adid = 956;

final List<Integer> pubchemAidList = [1018,1513,1514,1449,1750] as List<Integer>  //1746 already present

final String username = "dlahr"

final String ownerDisplayName = "Burnham"

final Long projectId = 1413; //optional



Map<Integer, String> aidExtRefMap = pubchemAidList.collectEntries {[((Integer)it): "aid=$it".toString()]}


Set<ExternalReference> alreadyExist = ExternalReference.findAllByExtAssayRefInList(new ArrayList<>(aidExtRefMap.values()))

if (alreadyExist.size() > 0) {
    println("Some of the AID's entered already exist in the system: $alreadyExist")
    return
}


Assay assay = Assay.findById(adid)
if (! assay) {
    println("could not find ADID:  $adid")
    return
}

Role ownerRole = Role.findByDisplayName(ownerDisplayName)
if (! ownerRole) {
    println("could not find owner:  $ownerDisplayName")
    return
}

ExternalSystem pubchemExternalSystem = ExternalSystem.findById(1)

PubChemXMLParserFactory pubChemXMLParserFactory = PubChemXMLParserFactory.getInstance()

List<Experiment> newExperimentList = new ArrayList<Experiment>(aidExtRefMap.keySet().size())

for (Integer aid : aidExtRefMap.keySet()) {
    Experiment e = new Experiment()
    newExperimentList.add(e)

    e.ownerRole = ownerRole
    e.modifiedBy = username
    e.assay = assay


    ExternalReference er = new ExternalReference()
    er.modifiedBy = username
    er.extAssayRef = aidExtRefMap.get(aid)
    er.externalSystem = pubchemExternalSystem

    e.addToExternalReferences(er)


    PCAssay pcAssay = pubChemXMLParserFactory.parsePublicAssayDescription(aid)
    e.experimentName = pcAssay.getName()
    e.holdUntilDate = pcAssay.holdUntilDate

    ExperimentDocument ed = new ExperimentDocument()
    ed.documentType = DocumentType.DOCUMENT_TYPE_DESCRIPTION
    ed.documentContent = pcAssay.getDescription()
    ed.documentName = "Description"
    ed.modifiedBy = username
    e.addToExperimentDocuments(ed)

    ed = new ExperimentDocument()
    ed.documentType = DocumentType.DOCUMENT_TYPE_PROTOCOL
    ed.documentContent = pcAssay.getProtocol()
    ed.documentName = "Protocol"
    ed.modifiedBy = username
    e.addToExperimentDocuments(ed)

    ed = new ExperimentDocument()
    ed.documentType = DocumentType.DOCUMENT_TYPE_COMMENTS
    ed.documentContent = pcAssay.getComment()
    ed.documentName = "Comment"
    ed.modifiedBy = username
    e.addToExperimentDocuments(ed)



    for(PCAssayXRef axref: pcAssay.getAssayXRefs()) {
//        println("axref:")
//        println("${axref.comment} \n ${axref.isTarget()} \n ${axref.taxonCommon} \n ${axref.taxonName}")
//        println()

        XRef xref = axref.getXRef();



        if( xref.getDatabase().equals("pubmed") && xref.getType().equals("pmid")) {
//            println("xref:")
//            println("${xref.database} \n ${xref.name} \n ${xref.type} \n ${xref.XRefId}")
//            println()



            ed = new ExperimentDocument()
            ed.documentType = DocumentType.DOCUMENT_TYPE_PUBLICATION
            ed.documentContent = "${pubmedUrlPrefix}${xref.getXRefId()}".toString()

            if (axref.comment != null && !axref.comment.trim().equals("")) {
                ed.documentName = axref.comment
            } else {
                ed.documentName = "Publication"
            }
            ed.modifiedBy = username
            e.addToExperimentDocuments(ed)
        }
    }

    e.validate()
    println(e.errors)
    assert(e.save())
}


if (projectId) {
    Project p = Project.findById(projectId)

    if (!p) {
        println("could not find project: $projectId")
        return
    }

    for (Experiment e : newExperimentList) {
        ProjectSingleExperiment pse = new ProjectSingleExperiment()
        pse.experiment = e
        pse.project = p
        pse.modifiedBy = username

        e.addToProjectExperiments(pse)
        p.addToProjectExperiments(pse)

        pse.validate()
        println(pse.errors)
        assert(pse.save())
    }
}




println("Fin!")