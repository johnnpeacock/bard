import bard.core.rest.spring.CompoundRestService
import bard.core.rest.spring.ProjectRestService
import org.springframework.web.client.RestTemplate
import bard.core.rest.spring.AssayRestService
import bard.core.rest.spring.ExperimentRestService
import bard.core.rest.spring.RestCombinedService
import bard.core.rest.spring.RestCombinedService

/**
 * Spring Configuration of resources
 */
beans = {

    final String ncgcBaseURL = grailsApplication.config.ncgc.server.root.url
    final String badApplePromiscuityUrl = grailsApplication.config.promiscuity.badapple.url
    restTemplate(RestTemplate) {
    }
    compoundRestService(CompoundRestService) {
        baseUrl = ncgcBaseURL
        promiscuityUrl = badApplePromiscuityUrl
        restTemplate = ref('restTemplate')
    }

    experimentRestService(ExperimentRestService) {
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
    }
    projectRestService(ProjectRestService) {
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
    }
    assayRestService(AssayRestService) {
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
    }
    restCombinedService(RestCombinedService){
        baseUrl = ncgcBaseURL
        restTemplate = ref('restTemplate')
        experimentRestService=ref('experimentRestService')
        compoundRestService=ref('compoundRestService')
        assayRestService=ref('assayRestService')
        projectRestService=ref('projectRestService')
    }
}
