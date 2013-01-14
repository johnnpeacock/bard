<div id="cardHolder" class="span12">
    <g:each in="${contexts}" var="entry">
        <div id="${entry.key}" class="roundedBorder card-group ${entry.key.replaceAll(/( |> )/, '-')}">
            <div class="row-fluid">
                <strong class="span12">${entry.key}</strong>
            </div>
            <div class="row-fluid">
                <g:each in="${contextOwner.splitForColumnLayout(entry.value)}" var="contextColumnList">
                    <div class="span6">
                        <g:each in="${contextColumnList}" var="context">
                            <g:render template="../contextItem/${subTemplate}"
                                      model="[contextOwner: contextOwner, context: context]"/>
                        </g:each>
                    </div>
                </g:each>
            </div>
        </div>
    </g:each>
</div>