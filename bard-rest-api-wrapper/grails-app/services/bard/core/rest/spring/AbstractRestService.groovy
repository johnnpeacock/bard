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

package bard.core.rest.spring

import bard.core.SearchParams
import bard.core.SuggestParams
import bard.core.exceptions.RestApiException
import bard.core.helper.LoggerService
import bard.core.interfaces.RestApiConstants
import bard.core.rest.spring.util.ETag
import bard.core.rest.spring.util.ETagCollection
import bard.core.rest.spring.util.Facet
import bard.core.util.ExternalUrlDTO
import bard.core.util.FilterTypes
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.StopWatch
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriTemplate

abstract class AbstractRestService {
    RestTemplate restTemplate
    final static int multiplier = 5
    LoggerService loggerService
    ExternalUrlDTO externalUrlDTO

    /**
     * @param params
     * @return String
     * @throws UnsupportedEncodingException
     */
    protected String buildSuggestQuery(SuggestParams params) throws UnsupportedEncodingException {
        return new StringBuilder(externalUrlDTO.ncgcUrl).
                append(RestApiConstants.FORWARD_SLASH).
                append(RestApiConstants.SEARCH).
                append(getResourceContext()).
                append(RestApiConstants.FORWARD_SLASH).
                append(RestApiConstants.SUGGEST).
                append(RestApiConstants.QUESTION_MARK).
                append(RestApiConstants.SOLR_QUERY_PARAM_NAME).
                append(URLEncoder.encode(params.getQuery(), RestApiConstants.UTF_8)).
                append(RestApiConstants.TOP).append(params.getNumSuggestion()).toString();
    }

    String buildExperimentQuery(Long experimentId, String etag, Integer top, Integer skip, List<FilterTypes> filterType) {
        final StringBuilder resource = new StringBuilder(getResource(experimentId.toString()));

        if (etag) {
            resource.append(RestApiConstants.FORWARD_SLASH)
            resource.append(RestApiConstants.ETAG).
                    append(RestApiConstants.FORWARD_SLASH).
                    append(etag);
        }
        resource.append(RestApiConstants.EXPTDATA_RESOURCE).
                append(RestApiConstants.QUESTION_MARK);
        if (top) {
            resource.append(RestApiConstants.SKIP).
                    append(skip).
                    append(RestApiConstants.TOP).
                    append(top).
                    append(RestApiConstants.AMPERSAND)
        }
        if (!filterType.contains(FilterTypes.TESTED)) {
            resource.append(RestApiConstants.FILTER).
                    append(RestApiConstants.ACTIVE).
                    append(RestApiConstants.AMPERSAND)
        }
        resource.append(RestApiConstants.EXPAND_TRUE);
        return resource.toString();
    }

    /**
     * @param top
     * @param skip
     * @return String
     */
    protected String buildQueryForCollectionOfETags(long top, long skip) {
        return new StringBuilder(getResource()).
                append(RestApiConstants.ETAG).
                append(RestApiConstants.QUESTION_MARK).
                append(RestApiConstants.SKIP).
                append(skip).
                append(RestApiConstants.TOP).
                append(top).
                append(RestApiConstants.AMPERSAND).
                append(RestApiConstants.EXPAND_TRUE).
                toString();
    }

    protected String buildSearchByCapIdURLs(final List<Long> capIds, final SearchParams searchParams, final String prefix) {
        final List<String> queries = []
        for (Long capId : capIds) {
            queries.add(prefix + capId)
        }
        searchParams.query = queries.join(" or ")
        final String searchURL = buildSearchURL(searchParams)

        return searchURL
    }
    /**
     * @param top
     * @param skip
     * @return String
     */
    protected String buildQueryForETag(final SearchParams searchParams, final String etag) {
        if (etag) {
            final String resource = buildETagQuery(etag)
            return new StringBuilder(getResource(resource)).
                    append(RestApiConstants.QUESTION_MARK).
                    append(RestApiConstants.SKIP).
                    append(searchParams.skip).
                    append(RestApiConstants.TOP).
                    append(searchParams.top).
                    append(RestApiConstants.AMPERSAND).
                    append(RestApiConstants.EXPAND_TRUE).
                    toString();
        }
        return ""
    }

    /**
     * @param etag
     * @return String
     */
    protected final String buildETagQuery(final String etag) {
        return new StringBuilder(RestApiConstants.ETAG).
                append(RestApiConstants.FORWARD_SLASH).
                append(etag).
                toString();
    }

    /**
     *  Get the URL to get a Compound. This is  url template so replace {id} with the
     *  real ID
     * @return the url
     */
    public String buildEntityURL() {
        return new StringBuilder(getResource()).
                append("{id}").
                append(RestApiConstants.FORWARD_SLASH).
                toString();
    }

    public String buildURLToCreateETag() {
        return new StringBuilder(getResource()).append(RestApiConstants.ETAG).toString();
    }

    public String buildURLToPutETag() {
        return new StringBuilder(buildURLToCreateETag()).append(RestApiConstants.FORWARD_SLASH).append().toString()
    }

    public String buildURLToPostIds() {
        StringBuilder url = new StringBuilder(getResource())
        url.append(RestApiConstants.QUESTION_MARK).append(RestApiConstants.EXPAND_TRUE)
        return url.toString()
    }

    public String buildURLToPostSids() {
        StringBuilder url = new StringBuilder(getResource())
        url.append(RestApiConstants.SID)
        url.append(RestApiConstants.QUESTION_MARK).append(RestApiConstants.EXPAND_TRUE)
        return url.toString()
    }

    public String buildURLToGetSid(Long sid) {
        StringBuilder url = new StringBuilder(getResource())
        url.append(RestApiConstants.SID)
        url.append(RestApiConstants.FORWARD_SLASH)
        url.append(sid)
        url.append(RestApiConstants.QUESTION_MARK).append(RestApiConstants.EXPAND_TRUE)
        return url.toString()
    }

    protected String getResource(final String resource) {
        return new StringBuilder(getResource()).append(resource).toString();
    }
    // TODO: This method no longer needed. We need to pass top and skip to all
    //resources. So no need to CAP
    protected int findNextTopValue(long skip, int ratio) {
        ///cap this at 1000
        if (skip > 1000) {
            return 1000;
        }
        return ratio;
    }

    /**
     * @param resource
     * @param expand
     * @param top
     * @param skip
     * @return
     */
    protected String addTopAndSkip(final String resource,
                                   final boolean expand,
                                   final long top = 10,
                                   final long skip = 0) {
        return new StringBuilder(resource).
                append(!resource.contains(RestApiConstants.QUESTION_MARK) ? RestApiConstants.QUESTION_MARK : RestApiConstants.AMPERSAND).
                append(RestApiConstants.SKIP).
                append(skip).
                append(RestApiConstants.TOP).
                append(top).
                append(expand ? (RestApiConstants.AMPERSAND + RestApiConstants.EXPAND_TRUE) : "").toString();
    }


    static String getParentETag(Map<String, Long> etags) {
        String mintag = "";
        Long minval = null;
        for (String key : etags.keySet()) {
            final long value = etags.get(key)
            if (!minval || minval > value) {
                mintag = key;
                minval = value;
            }
        }
        return mintag;
    }

    public void extractETagsFromResponseHeader(final HttpHeaders headers, final long skip, final Map<String, Long> etags) {
        if (headers.containsKey(RestApiConstants.E_TAG) && etags != null) {
            final String etag = headers.getFirst(RestApiConstants.E_TAG)
            String e = etag.replaceAll("\"", "")
            etags.put(e, skip);
        }
    }

    public void addETagsToHTTPHeader(HttpHeaders requestHeaders, final Map<String, Long> etags) {
        if (etags) {
            String etag = getParentETag(etags);
            requestHeaders.set("If-Match", "\"" + etag + "\"");
        }
    }
    /**
     * Build a search url from the params
     * @param searchParams
     * @return a fully encoded search url
     */
    public String buildSearchURL(SearchParams searchParams) {
        final StringBuilder urlBuilder = new StringBuilder()
        urlBuilder.append(getSearchResource()).
                append(RestApiConstants.SOLR_QUERY_PARAM_NAME).
                append(URLEncoder.encode(searchParams.getQuery(), RestApiConstants.UTF_8));
        urlBuilder.append(buildFilters(searchParams));
        if (searchParams.getFilters()) {
            urlBuilder.append(RestApiConstants.COMMA);
        }

        if (searchParams.getSkip() || searchParams.getTop()) {
            return addTopAndSkip(urlBuilder.toString(), true, searchParams.getTop(), searchParams.getSkip())
        }
        return urlBuilder.toString();

    }
    /**
     * @param params
     * @return String
     */
    protected String buildFilters(SearchParams params) {
        final StringBuilder f = new StringBuilder("");
        if (params.getFilters()) {
            f.append(RestApiConstants.AMPERSAND_FILTER);
            String sep = "";
            for (String[] entry : params.getFilters()) {
                f.append(sep).
                        append(RestApiConstants.FQ).append(RestApiConstants.LEFT_PAREN).
                        append(URLEncoder.encode(entry[0], RestApiConstants.UTF_8)).
                        append(RestApiConstants.COLON)
                        .append(URLEncoder.encode(entry[1], RestApiConstants.UTF_8))
                        .append(RestApiConstants.RIGHT_PAREN);
                sep = RestApiConstants.COMMA;
            }
        }
        return f.toString();
    }
    /**
     * @param params
     * @return String
     * For example : http://bard.nih.gov/api/v15/experiments/11795/exptdata?expand=true&filter=active
     */
    protected String buildFiltersForEntitySearch(SearchParams params) {
        final StringBuilder f = new StringBuilder("");
        if (params.getFilters()) {
            f.append(RestApiConstants.AMPERSAND_FILTER);
            String sep = "";
            for (String[] entry : params.getFilters()) {
                f.append(sep).
                        append(RestApiConstants.FQ).append(RestApiConstants.LEFT_PAREN).
                        append(URLEncoder.encode(entry[0], RestApiConstants.UTF_8)).
                        append(RestApiConstants.COLON)
                        .append(URLEncoder.encode(entry[1], RestApiConstants.UTF_8))
                        .append(RestApiConstants.RIGHT_PAREN);
                sep = RestApiConstants.COMMA;
            }
        }
        return f.toString();
    }

    public Map<String, List<String>> suggest(SuggestParams params) {
        final String resource = buildSuggestQuery(params)
        final URL url = new URL(resource)
        final Map<String, List<String>> suggestions = (Map) getForObject(url.toURI(), Map.class)
        return suggestions;
    }

    public List<ETag> findAllETagsForResource() {
        List<ETag> etags = new ArrayList<ETag>()
        int top = multiplier * multiplier;
        int ratio = multiplier;
        int skip = 0;
        while (true) {
            etags.addAll(getETags(top, skip));
            skip += etags.size();
            ratio *= multiplier;
            if (etags.size() < top) {
                break;
            }
            top = findNextTopValue(skip, ratio);
        }
        return etags;
    }

    public List<ETag> getETags(long top, long skip) {
        final String resource = buildQueryForCollectionOfETags(top, skip);
        final URL url = new URL(resource)
        //Using ETag[] to get around issue reported here : https://jira.springsource.org/browse/SPR-7002
        ETagCollection eTagCollection = (ETagCollection) getForObject(url.toURI(), ETagCollection.class)

        return eTagCollection.etags
    }


    public List<Facet> getFacetsByETag(String etag) {
        final String resource = buildETagQuery(etag) + RestApiConstants.FORWARD_SLASH + RestApiConstants.FACETS

        final String urlString = getResource(resource);
        final URL url = new URL(urlString)
        //Using Facte[] to get around issue reported here : https://jira.springsource.org/browse/SPR-7002
        final List<Facet> facets = (getForObject(url.toURI(), Facet[].class)) as List<Facet>

        return facets;
    }

    public String newETag(final String name, final List<Long> ids) {
        final String url = this.buildURLToCreateETag()
        try {
            final Map<String, Long> etags = [:]
            final MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            if (ids) {
                map.add("ids", ids.join(","));
            }
            map.add("name", name)

            final HttpEntity entity = new HttpEntity(map, new HttpHeaders());


            final HttpEntity exchange = restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
            HttpHeaders headers = exchange.getHeaders()

            this.extractETagsFromResponseHeader(headers, 0, etags)

            // there should only be one ETag returned
            return firstETagFromMap(etags)
        } catch (HttpClientErrorException httpClientErrorException) { //throws a 4xx exception
            log.error(url.toString(), httpClientErrorException)
            throw httpClientErrorException
        } catch (RestClientException restClientException) {
            log.error(url.toString(), restClientException)
            throw new RestApiException(restClientException)
        }
    }
    /**
     *   We will only get the first etag from the map, actually right now only one is returned
     *
     */
    protected String firstETagFromMap(final Map<String, Long> etags) {
        if (!etags.isEmpty()) {
            return etags.keySet().iterator().next().toString()
        }
        return null
    }

    protected void validatePutETag(final String etag, final List<Long> ids) {
        if (StringUtils.isBlank(etag) || !ids) {
            final String message = "etag value and id list is expected";
            log.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    public int putETag(final String etag, final List<Long> ids) {
        validatePutETag(etag, ids)
        final String url = this.buildURLToPutETag() + etag

        try {

            final MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("ids", ids.join(","));

            final HttpEntity<Integer> entity = new HttpEntity(map, new HttpHeaders());
            final HttpEntity<String> exchange = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            Integer count = Integer.parseInt(exchange.getBody())
            return count;
        } catch (HttpClientErrorException httpClientErrorException) { //throws a 4xx exception
            log.error(url.toString(), httpClientErrorException)
            throw httpClientErrorException
        } catch (RestClientException restClientException) {
            log.error(url.toString(), restClientException)
            throw new RestApiException(restClientException)
        }
    }

    /**
     * Get a count of entities making up a resource
     * @return the number of  entities
     */
    public long getResourceCount() {
        final String resource = getResource(RestApiConstants._COUNT);
        return getResourceCount(resource);
    }
    /**
     * Get a count of entities making up a resource
     * @return the number of  entities
     */
    public long getResourceCount(final String resource) {
        try {
            final URL url = new URL(resource)
            final String countString = (String) getForObject(url.toURI(), String.class)
            Long count = Long.parseLong(countString)
            return count;
        }
        catch (Exception ee) {
            log.error(ee, ee)
            return -1
        }
    }
    /**
     * Get a count of entities making up a resource
     * @return the number of  entities
     */
    public long getResourceCount(final SearchParams searchParams) {
        final StringBuilder resource = new StringBuilder(getResource(RestApiConstants._COUNT));
        if (searchParams.getTop()) {
            resource.append(RestApiConstants.QUESTION_MARK);
            resource.append(RestApiConstants.SKIP).
                    append(searchParams.getSkip()).
                    append(RestApiConstants.TOP).
                    append(searchParams.getTop())
        }
        resource.append(buildFilters(searchParams))
        return getResourceCount(resource.toString())
    }

    public <T> T getForObject(URI uri, Class<T> clazz) {
        StopWatch sw = this.loggerService.startStopWatch()
        try {
            ResponseEntity<T> responseEntity = this.restTemplate.getForEntity(uri, clazz)
            T result = responseEntity.body
            this.loggerService.stopStopWatch(sw, responseEntity.statusCode, HttpMethod.GET, uri.toString())
            return result
        }
        catch (HttpClientErrorException httpClientErrorException) { //throws a 4xx exception
            this.loggerService.stopStopWatch(sw, httpClientErrorException.statusCode, HttpMethod.GET, uri.toString())
            throw httpClientErrorException
        } catch (HttpServerErrorException httpServerErrorException) { // 5xx exception
            this.loggerService.stopStopWatch(sw, httpServerErrorException.statusCode, HttpMethod.GET, uri.toString())
            throw new RestApiException(httpServerErrorException)
        } catch (RestClientException restClientException) {
            this.loggerService.stopStopWatchError(sw, null, HttpMethod.GET, uri.toString(), restClientException)
            throw new RestApiException(restClientException)
        }
    }

    public <T> T getForObject(final String uriString, final Class<T> clazz, Map map = [:]) {
        URI uri = new UriTemplate(uriString).expand(map)
        return getForObject(uri, clazz)
    }

    public <T> T  postForObject(final URI uri, final Class<T> clazz, Map map = [:]) {
        StopWatch sw = this.loggerService.startStopWatch()
        String logMessage = "${uri} <${map}>"
        try {
            ResponseEntity<T> responseEntity = this.restTemplate.postForEntity(uri, map, clazz)
            T result = responseEntity.body
            this.loggerService.stopStopWatch(sw, responseEntity.statusCode, HttpMethod.POST, logMessage)
            return result
        }
        catch (HttpClientErrorException httpClientErrorException) { //throws a 4xx exception
            this.loggerService.stopStopWatch(sw, httpClientErrorException.statusCode, HttpMethod.POST, logMessage)
            throw httpClientErrorException
        } catch (HttpServerErrorException httpServerErrorException) { // 5xx exception
            this.loggerService.stopStopWatch(sw, httpServerErrorException.statusCode, HttpMethod.POST, logMessage)
            throw new RestApiException(httpServerErrorException)
        }
        catch (RestClientException restClientException) {
            this.loggerService.stopStopWatchError(sw, null, HttpMethod.POST, logMessage, restClientException)
            throw new RestApiException(restClientException)
        }
    }

    public <T> ResponseEntity<T> postExchange(String url, HttpEntity<?> entity, Class<T> clazz) {
        StopWatch sw = this.loggerService.startStopWatch()
        String logMessage = "${url} ${entity}"
        try {
            ResponseEntity<T> result = restTemplate.exchange(url, HttpMethod.POST, entity, clazz);
            this.loggerService.stopStopWatch(sw, result.statusCode, HttpMethod.POST, logMessage)
            return result
        }
        catch (HttpClientErrorException httpClientErrorException) { //throws a 4xx exception
            this.loggerService.stopStopWatch(sw, httpClientErrorException.statusCode, HttpMethod.POST, logMessage)
            throw httpClientErrorException
        } catch (HttpServerErrorException httpServerErrorException) { // 5xx exception
            this.loggerService.stopStopWatch(sw, httpServerErrorException.statusCode, HttpMethod.POST, logMessage)
            throw new RestApiException(httpServerErrorException)
        }
        catch (RestClientException restClientException) {
            this.loggerService.stopStopWatchError(sw, null, HttpMethod.POST, logMessage, restClientException)
            throw new RestApiException(restClientException)
        }
    }

    public <T> ResponseEntity<T> getExchange(URI uri, HttpEntity<?> entity, Class<T> clazz) {
        StopWatch sw = this.loggerService.startStopWatch()
        String logMessage = "${uri} ${entity}"
        try {
            ResponseEntity<T> result = restTemplate.exchange(uri, HttpMethod.GET, entity, clazz);
            this.loggerService.stopStopWatch(sw, result.statusCode, HttpMethod.GET, logMessage)
            return result
        } catch (HttpClientErrorException httpClientErrorException) { //throws a 4xx exception
            this.loggerService.stopStopWatch(sw, httpClientErrorException.statusCode, HttpMethod.GET, logMessage)
            throw httpClientErrorException
        } catch (HttpServerErrorException httpServerErrorException) { // 5xx exception
            this.loggerService.stopStopWatch(sw, httpServerErrorException.statusCode, HttpMethod.GET, logMessage)
            throw new RestApiException(httpServerErrorException)
        } catch (RestClientException restClientException) {
            this.loggerService.stopStopWatchError(sw, null, HttpMethod.GET, logMessage, restClientException)
            throw new RestApiException(restClientException)
        }
    }


    public abstract String getResource();

    public abstract String getSearchResource();

    public abstract String getResourceContext();
}
