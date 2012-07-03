<g:if test="${totalCompounds > 0}">
    <h3><a href="#">Compounds (${totalCompounds})</a></h3>

</g:if>
<g:else>
    <h3><a href="#">Compounds (${compounds.size()})</a></h3>
</g:else>

<div>
    <div class="content">
        <g:each var="compound" in="${compounds}">
            <g:link controller="bardWebInterface" action="showCompound" params="[cid: compound]">${compound}</g:link>
        </g:each>
    </div>
    <g:if test="${totalCompounds}">
        <div class="pagination">
            <g:paginate total="${totalCompounds}" action="search"
                        params="[searchString: params?.searchString, searchType: 'COMPOUNDS']"/>
        </div>
    </g:if>
</div>