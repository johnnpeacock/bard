%{-- Extract this to a taglib to simplify the logic --}%
<%@ page import="bard.db.enums.Status; bard.db.enums.Status; org.apache.commons.lang3.BooleanUtils" %>
<dl>
    <dt><g:message code="assay.label" default="Assay Definition"/>:</dt>
    <dd>
        <ul>
            <li>
                <g:link controller="assayDefinition" action="show"
                        id="${experiment?.assay?.id}">${experiment?.assay?.name}
                    <g:render template="/common/statusIcons" model="[status:experiment?.assay?.assayStatus?.id, entity: 'Assay']"/>
            </g:link>

            </li>
        </ul>
    </dd>
    <g:if test="${excludedLinks?.contains('experiment.show') == false}">

        <dt>Experiment Links:</dt>
        <dd>
            <ul>
                <li>
                    <g:link controller="experiment" action="show"
                            id="${experiment?.id}">View Experiment Details
                        <g:render template="/common/statusIcons" model="[status:experiment?.experimentStatus?.id, entity: 'Experiment']"/>
                    </g:link>
                </li>

            </ul>
        </dd>
    </g:if>
    <dt>Projects:</dt>
    <dd>
        <ul>
            <g:each in="${experiment?.projectExperiments}" var="projectExperiment">
                <g:if test="${projectExperiment.project.projectStatus != Status.RETIRED}">
                    <li><g:link controller="project" action="show"
                                id="${projectExperiment.project.id}">${projectExperiment.project.name}
                        <g:render template="/common/statusIcons" model="[status:projectExperiment?.project?.projectStatus?.id, entity: 'Project']"/>
                    </g:link></li>
                </g:if>
            </g:each>
        </ul>
    </dd>
    <dt>External references</dt>
    <dd>
        <ul>
            <g:each in="${experiment?.externalReferences}" var="xRef">
                <li>
                    <a href="${xRef.externalSystem.systemUrl}${xRef.extAssayRef}"
                       target="_blank">${xRef.externalSystem.systemName} ${xRef.extAssayRef}</a>
                </li>
            </g:each>
        </ul>
    </dd>
</dl>