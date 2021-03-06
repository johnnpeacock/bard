/* Copyright (c) 2014, The Broad Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The Broad Institute nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL The Broad Institute BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package bardqueryapi

import bard.core.SearchParams
import bard.core.adapter.AssayAdapter
import bard.core.adapter.CompoundAdapter
import bard.core.adapter.ExperimentAdapter
import bard.core.adapter.ProjectAdapter
import bard.core.rest.spring.assays.AbstractAssay
import bard.core.rest.spring.assays.Assay
import bard.core.rest.spring.assays.AssayResult
import bard.core.rest.spring.compounds.Compound
import bard.core.rest.spring.compounds.CompoundResult
import bard.core.rest.spring.experiment.Activity
import bard.core.rest.spring.experiment.ConcentrationResponseSeries
import bard.core.rest.spring.experiment.ExperimentSearchResult
import bard.core.rest.spring.experiment.PriorityElement
import bard.core.rest.spring.experiment.ResultData
import bard.core.rest.spring.project.Project
import bard.core.rest.spring.project.ProjectResult
import bard.core.rest.spring.util.MetaData
import bard.core.rest.spring.util.NameDescription
import bard.core.rest.spring.experiment.ExperimentSearch
import bard.db.enums.Status
import bard.db.experiment.Experiment
import org.apache.commons.lang3.text.WordUtils

import java.util.regex.Pattern

class QueryHelperService {
    /**
     * TODO: Put in properties file
     *  These are the terms that we would use for autosuggest
     *   Add more terms as we go along
     *  We should store this in a database so it becomes easy to manage
     */
    final static Map<String, String> AUTO_SUGGEST_FILTERS = [
            'gobp_term': 'GO Biological Process Term',
            'gocc_term': 'GO Cellular Component Term',
            'gomf_term': 'GO Molecular Function Term',
            'target_name': 'Target Name',
            'kegg_disease_cat': 'KEGG Disease Category',
            'kegg_disease_names': 'KEGG Disease Name',
            'assay_type': 'Assay Type',
            'iso_smiles': 'ISO SMILES',
            'iupac_name': 'IUPAC Name',
            'preferred_term': 'Preferred Term',
            'title': 'Title',
            'protocol': 'Protocol',
            'av_dict_label': 'Dictionary Value',
            'description': 'Description',
            'name': 'Name',
            'ak_dict_label': 'Dictionary Key',
            'class_name': 'Panther class name',
            'class_descr': 'Panther class description'
    ]
    final static String PROBE = "PROB"

    Map extractMapFromResultData(ResultData resultData, NormalizeAxis normalizeAxis) {
        if (resultData.hasPriorityElements()) {
            boolean hasPlot = false
            final List<PriorityElement> priorityElements = resultData.priorityElements

            final boolean hasChildElements = priorityElements.find {PriorityElement priorityElement -> priorityElement.hasChildElements()}
            final Map priorityMap = [:]
            if (resultData.hasPlot()) {
                hasPlot = true
            }
            if (normalizeAxis == NormalizeAxis.Y_NORM_AXIS) {
                if (resultData.hasConcentrationResponseSeries()) {
                    List<ConcentrationResponseSeries> concentrationResponseSeriesList = priorityElements*.getConcentrationResponseSeries()
                    if (concentrationResponseSeriesList) {
                        Double yNormMin = (concentrationResponseSeriesList*.sorterdActivities()).flatten().min()
                        Double yNormMax = (concentrationResponseSeriesList*.sorterdActivities()).flatten().max()
                        if (yNormMax && yNormMin) {
                            priorityMap.put("yNormMin", yNormMin)
                            priorityMap.put("yNormMax", yNormMax)
                        }
                    }

                }
            } else {
                priorityMap.put("yNormMin", null)
                priorityMap.put("yNormMax", null)

            }
            priorityMap.put("hasPlot", hasPlot)
            priorityMap.put("hasChildElements", hasChildElements)
            return priorityMap
        }
        return [hasPlot: false, hasChildElements: false, yNormMin: null, yNormMax: null]

    }

    Map extractExperimentDetails(List<Activity> activities, NormalizeAxis normalizeAxis = NormalizeAxis.Y_NORM_AXIS) {
        Double yNormMin = null
        Double yNormMax = null
        boolean firstMinValue = false
        boolean firstMaxValue = false
        boolean hasPlot = false
        boolean hasChildElements = false
        for (Activity activity : activities) {

            final ResultData resultData = activity.resultData

            if (resultData) {
                Map priorityMap = extractMapFromResultData(resultData, normalizeAxis)

                if (priorityMap.yNormMin) {
                    if (!firstMinValue) {  //if this is the first min value we are seeing
                        yNormMin = priorityMap.yNormMin
                        firstMinValue = true
                    }
                    else {
                        if (priorityMap.yNormMin < yNormMin) {
                            yNormMin = priorityMap.yNormMin
                        }
                    }
                }
                if (priorityMap.yNormMax) {
                    if (!firstMaxValue) { //if this is the first max value we are seeing
                        yNormMax = priorityMap.yNormMax
                        firstMaxValue = true
                    }
                    else {
                        if (priorityMap.yNormMax > yNormMax) {
                            yNormMax = priorityMap.yNormMax
                        }
                    }
                }
                if (priorityMap.hasPlot) {
                    hasPlot = true
                }
                if (priorityMap.hasChildElements) {
                    hasChildElements = true
                }
            }
        }
        return [hasPlot: hasPlot, hasChildElements: hasChildElements,
                yNormMin: yNormMin, yNormMax: yNormMax]
    }

    //filters that starts with a number or '[' to denote ranges
    final static Pattern FILTER_NUMBER_RANGES = Pattern.compile("^(\\d+.*|-\\d+.*)");

    public void matchMLPProbe(final String term, final List<Map<String, String>> autoSuggestTerms) {
        if (term.toUpperCase().contains(PROBE)) {
            final String label = "${term} as <strong> ML Probe</strong>"
            final String value = "ML_Probes"
            autoSuggestTerms.add([label: label, value: value])
        }
    }
    /**
     *
     * @param term
     * @param autoSuggestResponseFromJDO
     * @return the list of maps to use for auto suggest
     */
    public List<Map<String, String>> autoComplete(final String term, final Map<String, List<String>> autoSuggestResponseFromJDO) {
        final List<Map<String, String>> autoSuggestTerms = []
        if (term) {
            matchMLPProbe(term, autoSuggestTerms)
        }


        for (String key : autoSuggestResponseFromJDO.keySet()) {
            if (AUTO_SUGGEST_FILTERS.containsKey(key)) {
                final List<String> terms = autoSuggestResponseFromJDO.get(key)
                List<Map<String, String>> terms1 = this.getAutoSuggestTerms(AUTO_SUGGEST_FILTERS, terms, key)
                autoSuggestTerms.addAll(terms1)
            }
        }
        //we insert what the user has typed back into the map
        autoSuggestTerms.add(0, [label: term, value: term])
        //if the term is ML_Pro
        return autoSuggestTerms
    }
    /**
     * Apply the filters to the SearchParams
     * @param searchParams
     * @param searchFilters
     */
    void applySearchFiltersToSearchParams(final SearchParams searchParams, final List<SearchFilter> searchFilters) {
        if (searchFilters) {
            List<String[]> filters = []
            for (SearchFilter searchFilter : searchFilters) {
                filters.add([searchFilter.filterName, /"${searchFilter.filterValue}"/] as String[])
            }
            searchParams.setFilters(filters)
        }
    }

    /**
     *
     * @param searchString
     * @param top
     * @param skip
     * @param searchFilters {@link SearchFilter}'s
     * @return SearchParams
     */
    public SearchParams constructSearchParams(final String searchString, final Integer top, final Integer skip, final List<SearchFilter> searchFilters) {
        final SearchParams searchParams = new SearchParams(searchString)
        searchParams.setSkip(skip)
        searchParams.setTop(top);
        applySearchFiltersToSearchParams(searchParams, searchFilters)
        return searchParams

    }
    /**
     * @param searchFilters {@link SearchFilter}'s
     * return List<String[]>
     */
    public List<String[]> convertSearchFiltersToFilters(final List<SearchFilter> searchFilters) {
        List<String[]> filters = []
        for (SearchFilter searchFilter : searchFilters) {
            filters.add([searchFilter.filterName, searchFilter.filterValue] as String[])
        }
        return filters
    }
    //=========== Construct adapters ===================
    /**
     * Convert the list of compounds to the list of adapters
     * @param compounds {@link CompoundResult}'s
     * @return List of {@link CompoundAdapter}'s
     */
    final List<CompoundAdapter> compoundsToAdapters(final CompoundResult compoundResult) {
        final List<CompoundAdapter> compoundAdapters = []
        final MetaData metaData = compoundResult.metaData
        for (Compound compound : compoundResult.compounds) {
            Double score = null
            NameDescription nameDescription = null
            if (metaData) {
                score = metaData.getScore(compound.id.toString())
                nameDescription = metaData.getMatchingField(compound.id.toString())
            }
            final CompoundAdapter compoundAdapter = new CompoundAdapter(compound, score, nameDescription)
            compoundAdapters.add(compoundAdapter)
        }
        return compoundAdapters
    }
    /**
     * convert a list Assay's to a list of AssayAdapter's
     * @param assays {@link AssayResult}
     * @return list of {@link AssayAdapter}'s
     */
    public List<AssayAdapter> assaysToAdapters(final AssayResult assayResult) {
        final List<Assay> assays = assayResult.assays
        final MetaData metaData = assayResult.metaData
        if (assays) {
            return assaysToAdapters(assays, metaData)
        }
        return []
    }
    /**
     * convert a list Assay's to a list of AssayAdapter's
     * @param assays {@link Assay}
     * @param metaData {@link MetaData}
     * @return list of {@link AssayAdapter}'s
     */
    public List<AssayAdapter> assaysToAdapters(final List<AbstractAssay> assays, final MetaData metaData) {
        final List<AssayAdapter> assayAdapters = []
        for (AbstractAssay assay : assays) {
            Double score = null
            NameDescription nameDescription = null
            if (metaData) {
                score = metaData.getScore(assay.id.toString())
                nameDescription = metaData.getMatchingField(assay.id.toString())
            }
            final AssayAdapter assayAdapter = new AssayAdapter(assay, score, nameDescription)
            assayAdapters.add(assayAdapter)
        }
        return assayAdapters
    }

    public List<ProjectAdapter> projectsToAdapters(final ProjectResult projectResult) {
        final List<ProjectAdapter> projectAdapters = []
        final MetaData metaData = projectResult.metaData
        for (Project project : projectResult.projects) {
            Double score = null
            NameDescription nameDescription = null
            if (metaData) {
                score = metaData.getScore(project.id.toString())
                nameDescription = metaData.getMatchingField(project.id.toString())
            }
            String projectStatus = null
            if(project.capProjectId){
            bard.db.project.Project capProject = bard.db.project.Project.findById(project.capProjectId)
                projectStatus = capProject?.projectStatus?.id
            }
            final ProjectAdapter projectAdapter = new ProjectAdapter(project, score, nameDescription,null,projectStatus)
            projectAdapters.add(projectAdapter)
        }
        return projectAdapters
    }

    public List<ExperimentAdapter> experimentsToAdapters(final ExperimentSearchResult experimentSearchResult) {
        List<ExperimentAdapter> experimentAdapters = []
        final MetaData metaData = experimentSearchResult.metaData
        for(ExperimentSearch experimentSearch : experimentSearchResult.experiments){
            Double score = null
            NameDescription nameDescription = null
            if (metaData) {
                score = metaData.getScore(experimentSearch.bardExptId.toString())
                nameDescription = metaData.getMatchingField(experimentSearch.bardExptId.toString())
            }
            final ExperimentAdapter experimentAdapter = new ExperimentAdapter(experimentSearch, score, nameDescription)
            Experiment experiment = Experiment.get(experimentSearch.capExptId)
            if(experiment){
                experimentAdapter.ncgcWarehouseId = experiment.ncgcWarehouseId
                experimentAdapter.experimentFiles = experiment.experimentFiles?.size() > 0 ? true : false
                experimentAdapter.status = WordUtils.capitalize(experiment.experimentStatus.toString().toLowerCase())
                List<Long> projectIdList = experiment.projectExperiments.collect {it.project.id} as List
                if (projectIdList)
                    experimentAdapter.projectIdList = projectIdList
            }
            else{
                log.error("Error performing Experiment search. Experiment: ${experimentSearch?.capExptId} found in REST It is NOT found in CAP")
            }

            experimentAdapters.add(experimentAdapter)
        }
        return  experimentAdapters
    }

    /**
     * Extract filters from the search string if any
     * @param searchFilters {@link SearchFilter}'s
     * @param searchString
     * @return list of filters from search String
     */
    public void findFiltersInSearchBox(final List<SearchFilter> searchFilters, final String searchString) {
        final String filterName = findFilteredTerm(searchString)
        if (filterName) {
            final SearchFilter searchFilter = constructFilter(filterName, searchString)
            if (searchFilter) {
                searchFilters.add(searchFilter)
            }
        }
    }

    /**
     *
     * @param filtersMap
     * @param terms
     * @param currentAutoSuggestKey
     * @return Map of auto suggest terms
     */
    protected List<Map<String, String>> getAutoSuggestTerms(final Map<String, String> filtersMap,
                                                            final List<String> terms,
                                                            final String currentAutoSuggestKey) {

        final List<Map<String, String>> currentAutoSuggestTerms = []

        for (String term : terms) {
            final Map<String, String> termMap = constructSingleAutoSuggestTerm(filtersMap, currentAutoSuggestKey, term)
            if (termMap) {
                currentAutoSuggestTerms.add(termMap)
            }
        }
        return currentAutoSuggestTerms
    }

    /**
     * Return the part of the searchString that matched the filtered terms
     *
     * For example gobp_term:"Dna repair"
     * Is a filtered term and we should return  gobp_term
     * @param searchString
     * @return String
     */
    protected String findFilteredTerm(final String searchString) {
        if (searchString) {
            final Set<String> keys = AUTO_SUGGEST_FILTERS.keySet()
            //if the search string starts with anything in the FILTERS Map then we need to apply a filter to it
            final Collection<String> foundMatch = keys.findAll { key -> searchString.toLowerCase().startsWith(key) }
            if (foundMatch && !foundMatch.isEmpty()) {
                //just return the first item
                return foundMatch.iterator().next()
            }
        }
        return null
    }

    /**
     *
     *
     * @param filtersMap
     * @param currentAutoSuggestKey
     * @param term
     * @return the current map
     */
    protected Map<String, String> constructSingleAutoSuggestTerm(final Map<String, String> filtersMap, final String currentAutoSuggestKey, final String term) {
        if (currentAutoSuggestKey && term) {
            final String label = "${term} as <strong>" + filtersMap.get(currentAutoSuggestKey) + "</strong>"
            final String value = currentAutoSuggestKey + ":\"" + term + "\""
            return [label: label, value: value]
        }
        return [:]
    }

    /**
     * Add a filter name, value pair
     *
     * @param filterName
     * @param searchString
     */
    protected SearchFilter constructFilter(final String filterName, final String searchString) {
        if (filterName && searchString) {
            final String searchValue = stripCustomFiltersFromSearchString(searchString)
            if (searchValue) {
                return new SearchFilter(filterName: filterName, filterValue: searchValue)
            }
        }
        return null
    }
    /**
     *  Remove custom syntax from search string
     *  re-normalize the search string to strip out custom syntax (e.g gobp:SearchString now become SearchString)
     * @param searchString
     * @return updated string
     */
    protected String stripCustomStringFromSearchString(final String searchString) {
        final String updatedSearchString = stripCustomFiltersFromSearchString(searchString) ?: searchString
        return updatedSearchString
    }
    /**
     * If this string has custom search paramaters, remove them
     * @param searchString
     * @return String
     */
    protected String stripCustomFiltersFromSearchString(final String searchString) {
        if (searchString) {
            final int firstIndexOfColon = searchString.trim().indexOf(":")
            if (firstIndexOfColon > -1) { //if we found any string that has a colon in it, we assume that it is a filter
                return searchString.substring(firstIndexOfColon + 1, searchString.length())
            }
        }
        return null
    }


}
