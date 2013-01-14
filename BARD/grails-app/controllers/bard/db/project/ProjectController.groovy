package bard.db.project

import bard.db.experiment.Experiment
import grails.converters.JSON
import bard.db.registration.Assay

class ProjectController {
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
    ProjectExperimentRenderService projectExperimentRenderService
    ProjectService projectService

    def show() {
        def projectInstance = Project.get(params.id)
        if (!projectInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])
            return
        }
        [instance: projectInstance, pexperiment:projectExperimentRenderService.contructGraph(projectInstance)]
    }

    def edit() {
        def projectInstance = Project.get(params.id)

        if (!projectInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'project.label', default: 'Project'), params.id])
            return
        }
        else
            flash.message = null

        [instance: projectInstance, pexperiment:projectExperimentRenderService.contructGraph(projectInstance)]
    }

    def removeExperimentFromProject(Long experimentId, Long projectId) {
        def experiment = Experiment.findById(experimentId)
        def project = Project.findById(projectId)
        projectService.removeExperimentFromProject(experiment, project)
        project = Project.findById(projectId)
        // TODO: render template seemed not working, an alternative is modify the graph at the view, arbor provides function to prune node
        render(template: "showstep", model: [experiments: project.projectExperiments, pegraph: projectExperimentRenderService.contructGraph(project), instanceId: project.id])
    }

    def removeEdgeFromProject(Long fromExperimentId, Long toExperimentId, Long projectId) {
        def fromExperiment = Experiment.findById(fromExperimentId)
        def toExperiment = Experiment.findById(toExperimentId)
        def project = Project.findById(projectId)
        projectService.removeEdgeFromProject(fromExperiment, toExperiment, project)
        project = Project.findById(projectId)
        // TODO: render template seemed not working, an alternative is modify the graph at the view, arbor provides function to prune node
        render(template: "showstep", model: [experiments: project.projectExperiments, pegraph: projectExperimentRenderService.contructGraph(project), instanceId: project.id])
    }

    // Current the client send a list of displaynames of experiments.
    def associateExperimentsToProject() {
        // TODO: need to see why there is [] at the end of the parameter name
        def param1 = params['selectedExperiments[]']
        def projectId = params['projectId']
        def project = Project.findById(projectId)
        Set<String> selectedExperiments = new HashSet<String>()
        // ugly way to handle array being passed in as selected.
        if (param1 instanceof String) {
            selectedExperiments.add(param1)
        }else{
            param1.each{
                selectedExperiments.add(it)
            }
        }
        selectedExperiments.each{ String experimentDisplayName ->
            def experimentId = experimentDisplayName.split("-")[0]
            def experiment = Experiment.findById(experimentId)
            projectService.addExperimentToProject(experiment, project)
        }
        render(template: "showstep", model: [experiments: project.projectExperiments, pegraph: projectExperimentRenderService.contructGraph(project), instanceId: project.id])
    }

    def ajaxFindAvailableExperimentByName(String experimentName, Long projectId){
        List<Experiment> experiments = Experiment.findAllByExperimentNameIlike("%${experimentName}%")
        Project project = Project.findById(projectId)
        Set<Experiment> exps = []
        experiments.each{Experiment experiment ->
            if (!projectService.isExperimentAssociatedWithProject(experiment, project))
                exps.add(experiment)
        }
        render exps.collect {it.displayName} as JSON
    }

    def ajaxFindAvailableExperimentByAssayId(Long assayId, Long projectId){
        Assay assay = Assay.findById(assayId)
        Project project = Project.findById(projectId)
        List<Experiment> experiments = Experiment.findAllByAssay(assay)
        Set<Experiment> exps = []
        experiments.each{Experiment experiment ->
            if (!projectService.isExperimentAssociatedWithProject(experiment, project))
                exps.add(experiment)
        }
        render exps.collect {it.displayName} as JSON
    }

    def ajaxFindAvailableExperimentById(Long experimentId, Long projectId){
        Project project = Project.findById(projectId)
        Experiment experiment = Experiment.findById(experimentId)
        Set<Experiment> exps = []
        if (!projectService.isExperimentAssociatedWithProject(experiment, project))
            exps.add(experiment)
        render exps.collect {it.displayName} as JSON
    }
}

