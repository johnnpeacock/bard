
import bard.validation.ext.ExternalItem
import bard.db.registration.AssayContextItem
import bard.db.dictionary.Element
import bard.db.model.AbstractContextItem
import bard.db.project.ProjectContextItem
import bard.db.experiment.ExperimentContextItem
import org.apache.commons.lang3.StringUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import bard.db.audit.BardContextUtils
import bard.db.dictionary.OntologyDataAccessService

FileWriter logWriter = new FileWriter("populateExValueDisplay.out")

logWriter.write("ErrorType\tContextType\tContextItemId\tAttributeElementId\tURL\tExtValue\tModifiedBy\tErrorMessage\n")
logWriter.flush()
log = {msg ->
    //println(msg)
    logWriter.write(msg+"\n")
    logWriter.flush()
}

String modifiedBy = "fixExternalValueDisplay"

OntologyDataAccessService externalOntologyAccessService = ctx.ontologyDataAccessService
//List<AssayContextItem> acItems = AssayContextItem.findAllByExtValueIdIsNotNull()
List<AssayContextItem> acItems = AssayContextItem.executeQuery("""select aci from AssayContextItem as aci
join aci.assayContext as ac
join ac.assay as a
join a.experiments as experiment
join experiment.projectExperiments as pe
where aci.extValueId is not null and aci.modifiedBy <> '${modifiedBy}' and aci.modifiedBy <> 'BAO_1000'
""")

int updatedAssayContextItem = 0
for (AssayContextItem item : acItems) {
    if (processItem(item, externalOntologyAccessService, "Assay", modifiedBy)){
        BardContextUtils.setBardContextUsername(ctx.sessionFactory.currentSession, modifiedBy)
        item.save()
        updatedAssayContextItem++
    }
}
println("Finish process assay context item, total ${acItems.size()}, updated ${updatedAssayContextItem}")

//List<ExperimentContextItem> ecItems = ExperimentContextItem.findAllByExtValueIdIsNotNull()
List<ExperimentContextItem> ecItems = ExperimentContextItem.executeQuery("""select eci from ExperimentContextItem as eci
join eci.experimentContext as ec
join ec.experiment as e
join e.projectExperiments as pe
where eci.extValueId is not null and eci.modifiedBy <> '${modifiedBy}' and eci.modifiedBy <> 'BAO_1000'
""")
int updatedExperimentContextItem = 0
for (ExperimentContextItem item : ecItems) {
    if (processItem(item, externalOntologyAccessService, "Experiment", modifiedBy)){
        BardContextUtils.setBardContextUsername(ctx.sessionFactory.currentSession, modifiedBy)
//       println("username: " + BardContextUtils.getCurrentUsername(ctx.sessionFactory.currentSession))
        item.save()
        updatedExperimentContextItem++
    }
}
println("Finish process experiment context item, total ${ecItems.size()}, updated ${updatedExperimentContextItem}")

//List<ProjectContextItem> pcItems = ProjectContextItem.findAllByExtValueIdIsNotNull()
List<ProjectContextItem> pcItems =  ProjectContextItem.executeQuery("""select pci from ProjectContextItem as pci
join pci.context as pc
where pci.extValueId is not null and pci.modifiedBy <> '${modifiedBy}' and pci.modifiedBy <> 'BAO_1000'
""")
int updatedProjectContextItem = 0
for (ProjectContextItem item : pcItems) {
    if (processItem(item, externalOntologyAccessService, "Project", modifiedBy)){
        BardContextUtils.setBardContextUsername(ctx.sessionFactory.currentSession, modifiedBy)
 //       println("username: " + BardContextUtils.getCurrentUsername(ctx.sessionFactory.currentSession))
        item.save()
        updatedProjectContextItem++
    }
}
println("Finish process project context item, total ${pcItems.size()}, updated ${updatedProjectContextItem}")


boolean processItem(AbstractContextItem item, OntologyDataAccessService externalOntologyAccessService, String itemType, String modifiedBy) {
    Element element = item.attributeElement
    if (!element) {
        log("Missing attributeElement\t${itemType}ContextItem\t${item.id}\t\t\t${item.extValueId}\t${item.modifiedBy}\t\n")
        return false
    }
    if (!element.externalURL) {
        log("Missing externalURL\t${itemType}ContextItem\t${item.id}\t${element.id}\t\t${item.extValueId}\t${item.modifiedBy}\t\n")
        return false
    }
//    if (externalOntologyAccessService.externalOntologyHasIntegratedSearch(element.externalURL)){
//        println("No search implemented for ${element.externalURL}")
//        return false
//    }
    try{
        ExternalItem externalItem = externalOntologyAccessService.findExternalItemById(element.externalURL, StringUtils.trim(item.extValueId))
        if (!externalItem) {
            log("Not found\t${itemType}ContextItem\t${item.id}\t${element.id}\t${element.externalURL}\t${item.extValueId}\t${item.modifiedBy}\t\n")
            return false
        }
        if (externalItem.id != item.extValueId && externalItem.id != "GO:${item.extValueId}") {   // we know these go terms can be resolved this way, maybe better to add GO: in the field?
            log("Mismatched returnId\t${itemType}ContextItem\t${item.id}\t${element.id}\t${element.externalURL}\t${item.extValueId}\t${item.modifiedBy}\t\n")
            return false
        }
        if (externalItem.id == "GO:${item.extValueId}") {
            item.extValueId = externalItem.id
        }
        item.valueDisplay = externalItem.display
        item.modifiedBy = modifiedBy
        return true
    }catch(Exception e) {
        log("Exception\t${itemType}ContextItem\t${item.id}\t${element.id}\t${element.externalURL}\t${item.extValueId}\t${item.modifiedBy}\t${e.message}\n")
        return false
    }
}


