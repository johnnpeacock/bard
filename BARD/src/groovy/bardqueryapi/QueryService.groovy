package bardqueryapi

import bard.core.SearchParams
import bard.core.SuggestParams
import bard.core.Value
import bard.core.adapter.AssayAdapter
import bard.core.adapter.CompoundAdapter
import bard.core.adapter.ProjectAdapter
import bard.core.rest.spring.CapRestService
import bard.core.rest.spring.experiment.Activity
import bard.core.rest.spring.experiment.ExperimentData
import bard.core.rest.spring.experiment.ExperimentSearch
import bard.core.rest.spring.experiment.ExperimentShow
import bard.core.rest.spring.project.Project
import bard.core.rest.spring.project.ProjectExpanded
import bard.core.rest.spring.project.ProjectResult
import bard.core.rest.spring.util.StructureSearchParams
import bard.core.util.FilterTypes
import bardqueryapi.compoundBioActivitySummary.CompoundBioActivitySummaryBuilder
import bardqueryapi.experiment.ExperimentBuilder
import bard.core.rest.spring.*
import bard.core.rest.spring.assays.*
import bard.core.rest.spring.compounds.*
import bard.core.rest.spring.experiment.ExperimentSearchResult
import org.apache.commons.lang3.StringUtils
//import org.broadinstitute.ontology.GOOntologyService
import org.springframework.cache.annotation.Cacheable

class QueryService implements IQueryService {
    final static String PROBE_ETAG_ID = 'bee2c650dca19d5f'

    /**
     * {@link QueryHelperService}
     */
    QueryHelperService queryHelperService
    AssayRestService assayRestService
    CompoundRestService compoundRestService
    ProjectRestService projectRestService
    SubstanceRestService substanceRestService
    ExperimentRestService experimentRestService
    CapRestService capRestService
//    GOOntologyService goOntologyService

    //========================================================== Free Text Searches ================================

    Map searchCompoundsByCids(final List<Long> cids, final Integer top = 10, final Integer skip = 0, final List<SearchFilter> searchFilters = []) {

        final List<CompoundAdapter> foundCompoundAdapters = []
        Collection<Value> facets = []
        int nhits = 0
        String eTag = null
        if (cids) {

            final SearchParams searchParams = this.queryHelperService.constructSearchParams("", top, skip, searchFilters)
            //do the search
            CompoundResult compoundResult = compoundRestService.searchCompoundsByCids(cids, searchParams)

            //convert to adapters
            foundCompoundAdapters.addAll(this.queryHelperService.compoundsToAdapters(compoundResult))
            facets = compoundResult.getFacetsToValues()
            nhits = compoundResult.numberOfHits
            eTag = compoundResult.etag
        }
        return [compoundAdapters: foundCompoundAdapters, facets: facets, nHits: nhits, eTag: eTag]
    }
    /**
     * Find Compounds by Text search
     *
     * @param searchString
     * @param top
     * @param skip
     * @param searchFilters {@link SearchFilter}'s
     * @return Map of results
     */
    Map findCompoundsByTextSearch(final String searchString, final Integer top = 10, final Integer skip = 0, final List<SearchFilter> searchFilters = []) {
        final List<CompoundAdapter> foundCompoundAdapters = []
        Collection<Value> facets = []
        int nhits = 0
        String eTag = null
        if (searchString) {
            //re-normalize the search string to strip out custom syntax (e.g gobp:SearchString now become SearchString)
            String updatedSearchString = this.queryHelperService.stripCustomStringFromSearchString(searchString)

            final SearchParams searchParams = this.queryHelperService.constructSearchParams(updatedSearchString, top, skip, searchFilters)
            //do the search
            CompoundResult compoundResult = compoundRestService.findCompoundsByFreeTextSearch(searchParams)

            //convert to adapters
            foundCompoundAdapters.addAll(this.queryHelperService.compoundsToAdapters(compoundResult))
            facets = compoundResult.getFacetsToValues()
            nhits = compoundResult.numberOfHits
            eTag = compoundResult.etag
        }
        return [compoundAdapters: foundCompoundAdapters, facets: facets, nHits: nhits, eTag: eTag]
    }

    Map findAssaysByCapIds(final List<Long> capAssayIds, final Integer top = 10, final Integer skip = 0, final List<SearchFilter> searchFilters = []) {
        final List<AssayAdapter> foundAssayAdapters = []
        Collection<Value> facets = []
        int nhits = 0
        String eTag = null
        if (capAssayIds) {
            //TODO: Add filters
            final SearchParams searchParams = this.queryHelperService.constructSearchParams("", top, skip, searchFilters)
            final AssayResult assayResult = assayRestService.searchAssaysByCapIds(capAssayIds, searchParams)
            final List<AssayAdapter> adapters = this.queryHelperService.assaysToAdapters(assayResult)
            foundAssayAdapters.addAll(adapters)
            facets = assayResult.getFacetsToValues()
            nhits = assayResult.numberOfHits
            eTag = assayResult.etag
        }
        return [assayAdapters: foundAssayAdapters, facets: facets, nHits: nhits, eTag: eTag]
    }
    /**
     * @param searchString
     * @param top
     * @param skip
     * @param searchFilters {@link SearchFilter}'s
     * @return Map of results
     */
    Map findAssaysByTextSearch(final String searchString, final Integer top = 10, final Integer skip = 0, final List<SearchFilter> searchFilters = []) {
        final List<AssayAdapter> foundAssayAdapters = []
        Collection<Value> facets = []
        int nhits = 0
        String eTag = null
        if (searchString) {
            //re-normalize the search string to strip out custom syntax (e.g gobp:SearchString now become SearchString)
            String updatedSearchString = this.queryHelperService.stripCustomStringFromSearchString(searchString)

            final SearchParams searchParams = this.queryHelperService.constructSearchParams(updatedSearchString, top, skip, searchFilters)

            final AssayResult assayResult = assayRestService.findAssaysByFreeTextSearch(searchParams)
            final List<AssayAdapter> adapters = this.queryHelperService.assaysToAdapters(assayResult)
            foundAssayAdapters.addAll(adapters)
            facets = assayResult.getFacetsToValues()
            nhits = assayResult.numberOfHits
            eTag = assayResult.etag
        }
        return [assayAdapters: foundAssayAdapters, facets: facets, nHits: nhits, eTag: eTag]
    }
    /**
     * @param searchString
     * @param top
     * @param skip
     * @param searchFilters {@link SearchFilter}'s
     * @return Map of results
     */
    Map findProjectsByCapIds(final List<Long> capProjectIds, final Integer top = 10, final Integer skip = 0, final List<SearchFilter> searchFilters = []) {
        List<ProjectAdapter> foundProjectAdapters = []
        Collection<Value> facets = []
        int nhits = 0
        String eTag = null
        if (capProjectIds) {
            //TODO: Add filters
            final SearchParams searchParams = this.queryHelperService.constructSearchParams("", top, skip, searchFilters)
            final ProjectResult projectResult = projectRestService.searchProjectsByCapIds(capProjectIds, searchParams)
            final List<ProjectAdapter> adapters = this.queryHelperService.projectsToAdapters(projectResult)
            foundProjectAdapters.addAll(adapters)
            facets = projectResult.getFacetsToValues()
            nhits = projectResult.numberOfHits
            eTag = projectResult.etag
        }
        return [projectAdapters: foundProjectAdapters, facets: facets, nHits: nhits, eTag: eTag]
    }
    /**
     * @param searchString
     * @param top
     * @param skip
     * @param searchFilters {@link SearchFilter}'s
     * @return Map
     */
    Map findProjectsByTextSearch(final String searchString, final Integer top = 10, final Integer skip = 0, final List<SearchFilter> searchFilters = []) {
        List<ProjectAdapter> foundProjectAdapters = []
        Collection<Value> facets = []
        String eTag = null
        int nhits = 0
        if (searchString) {
            //query for count
            //re-normalize the search string to strip out custom syntax (e.g gobp:SearchString now becomes SearchString)
            String updatedSearchString = this.queryHelperService.stripCustomStringFromSearchString(searchString)
            final SearchParams searchParams = this.queryHelperService.constructSearchParams(updatedSearchString, top, skip, searchFilters)
            ProjectResult projectResult = projectRestService.findProjectsByFreeTextSearch(searchParams)
            foundProjectAdapters.addAll(this.queryHelperService.projectsToAdapters(projectResult))
            facets = projectResult.getFacetsToValues()
            nhits = projectResult.numberOfHits
            eTag = projectResult.etag
        }
        return [projectAdapters: foundProjectAdapters, facets: facets, nHits: nhits, eTag: eTag]
    }

    //====================================== Structure Searches ========================================
    Map structureSearch(Integer cid, StructureSearchParams.Type structureSearchParamsType, Double threshold = 0.90, List<SearchFilter> searchFilters = [], Integer top = 10, Integer skip = 0, Integer nhits = -1) {
        final Compound compound = this.compoundRestService.getCompoundById(cid)
        return structureSearch(compound.smiles, structureSearchParamsType, searchFilters, threshold, top, skip, nhits)
    }

    Map showProbeList() {
        final CompoundResult compoundResult = compoundRestService.findCompoundsByETag(PROBE_ETAG_ID)
        final List<CompoundAdapter> compoundAdapters = queryHelperService.compoundsToAdapters(compoundResult)
        return [
                compoundAdapters: compoundAdapters,
                facets: [],
                nhits: compoundAdapters.size(),
                appliedFilters: [:]
        ]

    }
    /**
     * @param smiles
     * @param structureSearchParamsType {@link StructureSearchParams}
     * @param top
     * @param skip
     * @return Map
     */
    Map structureSearch(String smiles, StructureSearchParams.Type structureSearchParamsType, List<SearchFilter> searchFilters = [], Double threshold = 0.90, Integer top = 10, Integer skip = 0, Integer nhits = -1) {
        final List<CompoundAdapter> compoundAdapters = []
        Collection<Value> facets = []
        int numHits = nhits
        String eTag = null
        if (smiles) {
            //construct search parameters
            final StructureSearchParams structureSearchParams =
                new StructureSearchParams(smiles, structureSearchParamsType).setSkip(skip).setTop(top);

            if (structureSearchParamsType == StructureSearchParams.Type.Similarity) {
                structureSearchParams.setThreshold(threshold)
            }
            if (searchFilters) {
                final List<String[]> filters = queryHelperService.convertSearchFiltersToFilters(searchFilters)
                structureSearchParams.setFilters(filters)
            }
            //do the search
            final CompoundResult compoundResult = compoundRestService.structureSearch(structureSearchParams);
            //collect the results
            //convert to adapters
            if (nhits <= 0) {
                numHits = compoundResult.numberOfHits
            }
            final List<CompoundAdapter> compoundsToAdapters = this.queryHelperService.compoundsToAdapters(compoundResult)
            if (compoundsToAdapters) {
                compoundAdapters.addAll(compoundsToAdapters)
            }

            //collect the facets
            //facets = searchIterator.facets
            eTag = compoundResult.etag
        }

        return [compoundAdapters: compoundAdapters, facets: facets, nHits: numHits, eTag: eTag]
    }
    /**
     *
     * @param cid
     * @return {@link CompoundSummary}
     */
    public CompoundSummary getSummaryForCompound(final Long cid) {
        return this.compoundRestService.getSummaryForCompound(cid)
    }

    /*
   * Returns an unexpanded list of sids
    */

    List<Long> findSubstancesByCid(Long cid) {
        if (cid) {
            return substanceRestService.findSubstancesByCid(cid)
        }
        return []
    }
    //===================== Find Resources given a list of IDs ================================
    /**
     * Given a list of Compound Ids return all the compounds that were found
     * @param compoundIds
     * @param filters {@link SearchFilter}'s  - We do not use filters because the JDO does not use them for ID searches yet
     * @return Map
     */
    Map findCompoundsByCIDs(final List<Long> compoundIds, List<SearchFilter> filters = []) {

        final List<CompoundAdapter> compoundAdapters = []
        Collection<Value> facets = []
        String eTag = null
        if (compoundIds) {
            //create ETAG using a random name
            //  eTag = restCompoundService.newETag("Compound ETags", compoundIds).toString();
            //commenting out facets until we figure out how to apply filters to ID searches
            //facets = restCompoundService.getFacets(etag)
            CompoundResult compoundResult = compoundRestService.searchCompoundsByIds(compoundIds)

            if (compoundResult) {
                compoundAdapters.addAll(this.queryHelperService.compoundsToAdapters(compoundResult))
                eTag = compoundResult.etag
            }
            //TODO: Even though facets are available they cannot be used for filtering
        }
        int nhits = compoundAdapters.size()
        return [compoundAdapters: compoundAdapters, facets: facets, nHits: nhits, eTag: eTag]
    }

    TableModel showExperimentalData(Long experimentId,
                                    GroupByTypes groupTypes,
                                    List<FilterTypes> filterTypes,
                                    SearchParams searchParams) {
        Integer top = searchParams.top
        Integer skip = searchParams.skip
        Map m = findExperimentDataById(experimentId, top, skip, filterTypes)
        ExperimentBuilder experimentBuilder = new ExperimentBuilder()
        return experimentBuilder.buildModel(m)

    }


    TableModel createCompoundBioActivitySummaryDataTable(Long compoundId,
                                                         GroupByTypes groupTypes,
                                                         List<FilterTypes> filterTypes,
                                                         List<SearchFilter> appliedSearchFilters,
                                                         SearchParams searchParams) {
        Integer top = searchParams.top
        Integer skip = searchParams.skip
        CompoundSummary compoundSummary = getSummaryForCompound(compoundId)
        List<Assay> testedAssays = compoundSummary.testedAssays
        List<Assay> hitAssays = compoundSummary.hitAssays

        List<Activity> experimentalData

        if (filterTypes.contains(FilterTypes.TESTED)) {
            //Get all tested assays and exptData
            experimentalData = compoundSummary.testedExptdata
        } else {
            //Get only the hits
            experimentalData = compoundSummary.hitExptdata
        }

        NormalizeAxis normalizeAxis = NormalizeAxis.Y_NORM_AXIS
        if (filterTypes.contains(FilterTypes.Y_DENORM_AXIS)) {
            normalizeAxis = NormalizeAxis.Y_DENORM_AXIS
        }
        Map experimentalDetails = this.queryHelperService.extractExperimentDetails(experimentalData, normalizeAxis)

        Map<Long, List<Activity>> groupedByExperimentalData = [:]
        switch (groupTypes) {
            case GroupByTypes.ASSAY:
                experimentalData.each { Activity exptData ->
                    exptData.bardAssayId.each { Long id ->
                        if (groupedByExperimentalData.containsKey(id)) {
                            groupedByExperimentalData[id] << exptData
                        } else {
                            groupedByExperimentalData.put(id, [exptData])
                        }
                    }
                }
                break;
            case GroupByTypes.PROJECT:
                experimentalData.each { Activity exptData ->
                    exptData.bardProjId.each { Long id ->
                        if (groupedByExperimentalData.containsKey(id)) {
                            groupedByExperimentalData[id] << exptData
                        } else {
                            groupedByExperimentalData.put(id, [exptData])
                        }
                    }
                }
                break;
            default:
                throw new RuntimeException("Group-by ${groupTypes} is not supported")
        }

        if (!groupedByExperimentalData) {
            return null
        }

        //Get a list of all experiment IDs so we can look them all up in one call to the REST API
        List<Long> eids = groupedByExperimentalData.values().toList().flatten().unique()*.bardExptId
        //Store them in a map for an easier lookup. Note that the values are arrays of a single element (ExperimentSearch).
        Map<Long, List<ExperimentSearch>> experimentsMap
        try {
            ExperimentSearchResult experimentSearchResult = this.experimentRestService.searchExperimentsByIds(eids)
            List<ExperimentSearch> experiments = experimentSearchResult.experiments
            //Create a map of experiment-id to experiment; please note that values in this map are Arraylists with one element in each.
            experimentsMap = experiments.groupBy { ExperimentSearch experiment -> experiment.bardExptId }
        }
        catch (Exception exp) {
            log.error("Could not find BARD experiment IDs: ${eids}")
        }

        //Sort the experiments in the map based on their confidenceLevel. This should give a higher priority, for example,
        // for confirmatory assays (dose-curve) over primary assay (single-point).
        //First sort all the elements (experiment's activities) in each key-set based on the experiment's confidence level
        groupedByExperimentalData.values().each { List<Activity> exptDataList ->
            exptDataList.sort { Activity lExptData, Activity rExptData ->
                Long lConfidenceLevel = experimentsMap[lExptData.bardExptId]?.first()?.confidenceLevel ?: 0
                Long rConfidenceLevel = experimentsMap[rExptData.bardExptId]?.first()?.confidenceLevel ?: 0
                return rConfidenceLevel <=> lConfidenceLevel
            }
        }
        //Then sort the resources (assays or projects) based on the first activity they have
        List<Long> sortedKeys = groupedByExperimentalData.keySet().sort { Long l, Long r ->
            ExperimentSearch lExperiment = experimentsMap[groupedByExperimentalData[l]?.first()?.bardExptId].first()
            ExperimentSearch rExperiment = experimentsMap[groupedByExperimentalData[r]?.first()?.bardExptId].first()
            return rExperiment?.confidenceLevel <=> lExperiment?.confidenceLevel
        }

        //Build the table
        CompoundBioActivitySummaryBuilder compoundBioActivitySummaryBuilder = new CompoundBioActivitySummaryBuilder(this)
        TableModel tableModel = compoundBioActivitySummaryBuilder.buildModel(groupTypes,
                groupedByExperimentalData,
                testedAssays,
                hitAssays,
                filterTypes,
                appliedSearchFilters,
                experimentsMap,
                sortedKeys,
                experimentalDetails?.yNormMin,
                experimentalDetails?.yNormMax)
        tableModel.additionalProperties.put('compoundSummary', compoundSummary)
        return tableModel
    }
    /**
     * Used for Show Experiment Page. Perhaps we should move this to the Query Service
     * @param experimentId
     * @param top
     * @param skip
     * @return Map of data to use to display an experiment
     */
    Map findExperimentDataById(Long experimentId, Integer top, Integer skip, List<FilterTypes> filterTypes = [FilterTypes.TESTED]) {
        List<Activity> activities = []
        final ExperimentShow experimentShow =
            experimentRestService.getExperimentById(experimentId)

        NormalizeAxis normalizeAxis = NormalizeAxis.Y_NORM_AXIS
        if (filterTypes.contains(FilterTypes.Y_DENORM_AXIS)) {
            normalizeAxis = NormalizeAxis.Y_DENORM_AXIS
        }
        long totalNumberOfRecords = experimentShow?.getCompounds() ?: 0
        long numberOfActiveCompounds = experimentShow?.getActiveCompounds() ?: 0
        Map experimentDetails = [:]
        Map<Long, CompoundAdapter> compoundAdaptersMap = [:]
        if (experimentShow) {
            //TODO: start using ETags

            final ExperimentData experimentData = experimentRestService.activities(experimentId, null, top, skip, filterTypes)
            activities = experimentData.activities

            experimentDetails = this.queryHelperService.extractExperimentDetails(activities, normalizeAxis)
            compoundAdaptersMap = this.getCompoundsForCIDS(activities)
        }

        return [
                total: totalNumberOfRecords,
                actives: numberOfActiveCompounds,
                activities: activities,
                experiment: experimentShow,
                hasPlot: experimentDetails.hasPlot,
                hasChildElements: experimentDetails.hasChildElements,
                yNormMin: experimentDetails.yNormMin,
                yNormMax: experimentDetails.yNormMax,
                normalizeYAxis: normalizeAxis,
                compoundAdaptersMap: compoundAdaptersMap
        ]
    }

    Map<Long, CompoundAdapter> getCompoundsForCIDS(List<Activity> activities) {
        final Map<Long, CompoundAdapter> compoundAdapterMaps = [:]
        final List<Long> cids = activities*.cid
        if (cids) {
            final Map ds = findCompoundsByCIDs(cids)
            final List<CompoundAdapter> compoundAdapters = ds.compoundAdapters
            for (CompoundAdapter compoundAdapter : compoundAdapters) {
                compoundAdapterMaps.put(compoundAdapter.id, compoundAdapter)
            }
        }
        return compoundAdapterMaps
    }
    /**
     * Given a list of Assay Ids return all the assays that were found
     * @param assayIds
     * @param filters {@link SearchFilter}'s  - We do not use filters because the JDO does not use them for ID searches yet
     * @return map
     */
    Map findAssaysByADIDs(final List<Long> assayIds, List<SearchFilter> filters = []) {
        final List<AssayAdapter> foundAssayAdapters = []
        Collection<Value> facets = []
        String eTag = null
        if (assayIds) {
            ExpandedAssayResult expandedAssayResult = this.assayRestService.searchAssaysByIds(assayIds)
            if (expandedAssayResult) {
                final List<ExpandedAssay> assays = expandedAssayResult.assays
                if (assays) {
                    foundAssayAdapters.addAll(this.queryHelperService.assaysToAdapters(expandedAssayResult.assays, expandedAssayResult.metaData))
                }
                eTag = expandedAssayResult.etag
                //TODO: Facet needed. Not yet ready in JDO
            }
        }
        final int nhits = foundAssayAdapters.size()
        return [assayAdapters: foundAssayAdapters, facets: facets, nHits: nhits, eTag: eTag]
    }

    /**
     *
     * Given a list of Project Ids return all the projects that were found
     * @param projectIds
     * @param filters {@link SearchFilter}'s   - We do not use filters because the JDO does not use them for ID searches yet
     * @return Map
     */
    Map findProjectsByPIDs(final List<Long> projectIds, List<SearchFilter> filters = []) {
        Collection<Value> facets = []
        final List<ProjectAdapter> foundProjectAdapters = []
        String eTag = null
        if (projectIds) {
            final ProjectResult projectResult = projectRestService.searchProjectsByIds(projectIds)
            if (projectResult) {
                final List<ProjectAdapter> projectsToAdapters = this.queryHelperService.projectsToAdapters(projectResult)
                if (projectsToAdapters) {
                    foundProjectAdapters.addAll(projectsToAdapters)
                }
                eTag = projectResult.etag
            }
        }
        final int nhits = foundProjectAdapters.size()
        return [projectAdapters: foundProjectAdapters, facets: facets, nHits: nhits, eTag: eTag]
    }

    //=============== Show Resources Given a Single ID ================

    /**
     * Given a CID, get detailed compound information from REST API
     * @param compoundId
     * @return CompoundAdapter
     */
    CompoundAdapter showCompound(final Long compoundId) {
        if (compoundId) {
            final Compound compound = compoundRestService.getCompoundById(compoundId)
            if (compound) {
                return new CompoundAdapter(compound)
            }
        }
        return null
    }

    /**
     * Given an assayId, get detailed Assay information from the REST API
     * @param assayId
     * @return Map
     */
    Map showAssay(final Long assayId) {
        if (assayId) {
            ExpandedAssay assay = assayRestService.getAssayById(assayId)
            List<ExperimentSearch> experiments = assay.experiments
            final List<Project> projects = assay.projects
            final BardAnnotation annotations = assayRestService.findAnnotations(assayId)
            final AssayAdapter assayAdapter = new AssayAdapter(assay, 0, null, annotations)
            return [assayAdapter: assayAdapter, experiments: experiments, projects: projects]
        }
        return [:]
    }
    /**
     * Given a projectId, get detailed Project information from the JDO
     * @param projectId
     * @return Map
     */
    Map showProject(final Long projectId) {
        if (projectId) {
            final ProjectExpanded project = projectRestService.getProjectById(projectId)
            if (project) {
                final BardAnnotation annotations = projectRestService.findAnnotations(projectId)
                final List<ExperimentSearch> experiments = project.experiments
                if (experiments) {
                    experiments.sort {
                        it.role
                    }
                }
                final List<Assay> assays = project.assays
                final ProjectAdapter projectAdapter = new ProjectAdapter(project, 0, null, annotations)
                return [projectAdapter: projectAdapter, experiments: experiments, assays: assays]
            }
        }
        return [:]
    }

    //==============================================Auto Complete ======
    /**
     *
     * @param term
     * @return the list of maps to use for auto suggest
     */
    public List<Map<String, String>> autoComplete(final String term) {

        //the number of items to retrieve per category
        final int numberOfTermsToRetrieve = 3
        //if string is already quoted strip it
        final String normalizedTerm = term.replaceAll("\"", "");

        final SuggestParams suggestParams = new SuggestParams(normalizedTerm, numberOfTermsToRetrieve)
        //we only use the terms in the assay service, because the other suggest services do not seem to
        //have useful things
        final Map<String, List<String>> autoSuggestResponseFromJDO = assayRestService.suggest(suggestParams);
        final List<Map<String, String>> autoSuggestTerms = this.queryHelperService.autoComplete(term, autoSuggestResponseFromJDO)
        return autoSuggestTerms
    }
    /**
     *
     * Extract filters from the search string if any
     * @param searchFilters {@link SearchFilter}'s
     * @param searchString
     * @return list of filters from search String
     */
    public void findFiltersInSearchBox(final List<SearchFilter> searchFilters, final String searchString) {
        queryHelperService.findFiltersInSearchBox(searchFilters, searchString)
    }
    /**
     *
     * @param cid
     * return Map
     * Success would return [status: 200, message: 'Success', promiscuityScore: promiscuityScore]
     * Failure would return [status: 404, message: "Error getting Promiscuity Score for ${CID}", promiscuityScore: null]
     * Use  findPromiscuityForCID(Long cid) instead
     */
    @Deprecated
    public Map findPromiscuityScoreForCID(Long cid) {
        final PromiscuityScore promiscuityScore = compoundRestService.findPromiscuityScoreForCompound(cid);
        if (promiscuityScore) {
            return [status: 200, message: 'Success', promiscuityScore: promiscuityScore]
        }
        return [status: 404, message: "Error getting Promiscuity Score for ${cid}", promiscuityScore: null]
    }
    /**
     *
     * @param cid
     * return Map
     * Success would return [status: 200, message: 'Success', promiscuityScore: promiscuityScore]
     * Failure would return [status: 404, message: "Error getting Promiscuity Score for ${CID}", promiscuityScore: null]
     */
    public Map findPromiscuityForCID(Long cid) {
        final Promiscuity promiscuity = compoundRestService.findPromiscuityForCompound(cid);
        if (promiscuity) {
            return [status: 200, message: 'Success', promiscuityScore: promiscuity]
        }
        return [status: 404, message: "Error getting Promiscuity Score for ${cid}", promiscuityScore: null]
    }
    /**
     *
     * @param mlNumber
     */
    @Override
    CompoundAdapter findProbe(String mlNumber) {
        Compound compound = compoundRestService.findProbe(mlNumber)
        if (compound) {
            return new CompoundAdapter(compound)
        }
        return null
    }

    @Override
    Map getPathsForAssayFormat(String endNode) {
        return getPathsForType('assayFormat', endNode)
    }

    @Override
    Map getPathsForAssayType(String endNode) {
        return getPathsForType('assayType', endNode)
    }

    Map getPathsForType(String type, String endNode) {
        Map dictionaryElementPaths = this.capRestService.getDictionaryElementPaths()
        List<String> paths = dictionaryElementPaths[type] ?: []
        String startingNode = type.replaceAll(/[A-Z]/, { (' ' + it[0]).toLowerCase() }) //convert assayType --> 'assay type'
        String foundPath = paths.find { it.endsWith(endNode + '/') }
        if (!foundPath) {
            return [:]
        }
        return buildPathMap(foundPath, startingNode)
    }

    private Map buildPathMap(String path, String startingNode) {
        String[] foundPathSplit = path.split('/')
        Integer startingNodeIndex = foundPathSplit.findIndexOf { it == startingNode } // e.g., find 'assay format' in '/BARD/assay protocol/assay format/biochemical format/nucleic acid format/'
        if (startingNodeIndex < 0) {
            return [:]
        }
        Map pathsForType = [:]
        for (Integer i in startingNodeIndex..(foundPathSplit.size() - 1)) {
            pathsForType.put(foundPathSplit[i], foundPathSplit[startingNodeIndex..i].join('/'))
        }
        return pathsForType
    }
}