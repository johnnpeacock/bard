<%@ page import="bard.db.dictionary.OntologyItem" %>
<!DOCTYPE html>
<html>
<head>
    <title>BARD: Dictionary Terms and Description</title>
    <r:require modules="dictionaryPage"/>
</head>

<body>

<div class="container-fluid">
    <div class="row-fluid">
        <g:render template="/layouts/templates/tableSorterTip"/>
        <table class="table table-striped table-hover table-bordered">
            <caption>Dictionary Terms and Description</caption>
            <thead>
            <tr>
                <th>ID</th>
                <th data-sort="string-ins">Term</th>
                <th>Description</th>
                <th data-sort="string-ins">Units</th>
                <th>Reference</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${capDictionary}" var="dictionaryElement">
                <g:if test="${dictionaryElement.label}">
                    <tr>
                        <td>
                            ${dictionaryElement.id}
                        </td>
                        <td>
                            <a name="${dictionaryElement.id}"></a>
                            ${dictionaryElement.label}
                        </td>
                        <td>
                            ${dictionaryElement.description}
                        </td>
                        <td>
                            ${dictionaryElement?.unit?.abbreviation}
                        </td>
                        <td>
                            <%
                                List<OntologyItem> ontologyItems = dictionaryElement.ontologyItems as List<OntologyItem>
                            %>
                            <g:each in="${ontologyItems}" var="ontologyItem">
                                ${ontologyItem.displayValue()}
                            </g:each>
                        </td>
                    </tr>
                </g:if>
            </g:each>
            </tbody>
        </table>

    </div>
</div>
<r:layoutResources/>
</body>
</html>

