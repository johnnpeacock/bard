<!DOCTYPE html>
<%@ page import="bard.core.rest.spring.assays.BardAnnotation; bardqueryapi.JavaScriptUtility; bard.db.registration.*" %>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="logoSearchCartAndFooter"/>
    <r:require modules="showProjectAssay,twitterBootstrapAffix"/>
    <title>BARD : Assay Definition : ADID ${assayAdapter?.capAssayId}</title>
</head>

<body>
<div class="row-fluid">
    <div class="span12 page-header">
        <h1>Assay Definition: ${assayAdapter?.name}
            <small>(ADID: ${assayAdapter?.capAssayId})</small>
        </h1>

        <g:saveToCartButton id="${assayAdapter.id}"
                            name="${JavaScriptUtility.cleanup(assayAdapter.name)}"
                            type="${querycart.QueryItemType.AssayDefinition}"/>
        <a class="btn btn-mini" href="${grailsApplication.config.bard.cap.assay}${assayAdapter?.capAssayId}"
           title="Click To Edit Assay Definition In Cap" rel="tooltip">Edit in CAP</a>
    </div>
</div>


<div class="row-fluid">
    <div class="span6">
        <g:render template="assaySummary" model="[assayAdapter: assayAdapter]"/>
    </div>

    <div class="span6">
        <dl>
            <dt>Associated Projects:</dt>
            <dd>
                <ul>
                    <g:each in="${projects}" var="project">
                        <li>

                        <g:if test="${searchString}">
                            <g:link controller="bardWebInterface" action="showProject" id="${project.id}"
                                    params='[searchString: "${searchString}"]'>${project.name}</g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="bardWebInterface" action="showProject" id="${project.id}">${project.name}</g:link>
                        </g:else>
                        </li>
                    </g:each>
                </ul>
            </dd>
        </dl>
    </div>
</div>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span3 bs-docs-sidebar">
            <ul class="nav nav-list bs-docs-sidenav twitterBootstrapAffixNavBar">
                <g:if test="${BardAnnotation.areAnnotationsEmpty(assayAdapter.annotations)}">
                    <li><a href="#assay-bio-info"><i class="icon-chevron-right"></i>Assay and Biology Details</a></li>
                </g:if>
                <g:if test="${BardAnnotation.areOtherAnnotationsEmpty(assayAdapter.annotations)}">
                    <li><a href="#assay-bio-info-misc"><i
                            class="icon-chevron-right"></i>Assay and Biology Details - Miscellaneous</a></li>
                </g:if>
                <g:if test="${assayAdapter.targets}">
                    <li><a href="#target-info"><i
                            class="icon-chevron-right"></i>Targets (${assayAdapter.targets.size()})</a></li>
                </g:if>
                <li><a href="#document-info"><i class="icon-chevron-right"></i>Documents</a></li>
                <g:if test="${assayAdapter.documents}">
                    <li><a href="#publication-info"><i
                            class="icon-chevron-right"></i>Publications (${assayAdapter.documents.size()})</a></li>
                </g:if>
                <li><a href="#result-info"><i class="icon-chevron-right"></i>Experiments (${experiments.size()})</a>
                </li>
            </ul>
        </div>

        <div class="span9">
            <g:if test="${BardAnnotation.areAnnotationsEmpty(assayAdapter.annotations)}">
                <section id="assay-bio-info">
                    <div class="page-header">
                        <h3>Assay and Biology Details</h3>
                    </div>

                    <div id="cardView" class="cardView" class="row-fluid">
                        <g:render template="listContexts" model="[annotations: assayAdapter.annotations]"/>
                    </div>

                </section>
            </g:if>
            <g:if test="${BardAnnotation.areOtherAnnotationsEmpty(assayAdapter.annotations)}">
                <section id="assay-bio-info-misc">
                    <div class="page-header">
                        <h3>Assay and Biology Details - Miscellaneous</h3>
                    </div>

                    <div id="cardViewMisc" class="cardView" class="row-fluid">
                        <g:render template="listMiscellaneous" model="[annotations: assayAdapter.annotations]"/>
                    </div>

                </section>
            </g:if>
            <g:if test="${assayAdapter.targets}">
                <g:render template="targets" model="['targets': assayAdapter.targets]"/>
            </g:if>
            <section id="document-info">
                <div class="page-header">
                    <h3>Documents
                        <small>(${[(assayAdapter.protocol ? 'protocol' : 'no protocol'),
                                (assayAdapter.description ? 'description' : 'no description'),
                                (assayAdapter.comments ? 'comments' : 'no comments')].join(', ')})</small></h3>
                </div>

                <g:render template="assayDocuments" model="['assayAdapter': assayAdapter]"/>

            </section>

            <g:if test="${assayAdapter.documents}">
                <g:render template="publications" model="['documents': assayAdapter.documents]"/>
            </g:if>
            <section id="result-info">
                <div class="page-header">
                    <h3>Experiments</h3>
                </div>
                <g:render template="experiments"
                          model="[experiments: experiments, showAssaySummary: false]"/>
            </section>
        </div>

    </div>
</div>
</body>
</html>