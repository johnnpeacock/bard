<%@ page import="bard.core.rest.spring.util.StructureSearchParams" %>

<script type="text/javascript" src="js/dojo-min/dojo/dojo.js"></script>
<script type="text/javascript" src="js/jsDraw/Scilligence.JSDraw2.js"></script>
<script type="text/javascript" src="js/jsDraw/license.js"></script>

%{--<r:require modules="structureSearch"/>--}%

<g:hiddenField name="searchTypes" id="searchTypes" value="${StructureSearchParams.Type.values().join(':')}"/>

<%-- MarvinSketch's modal window --%>
%{--<div class="modal hide" id="modalDiv">--}%
    %{--<div class="modal-header">--}%
        %{--<button type="button" class="close" data-dismiss="modal">×</button>--}%

        %{--<h3>Draw or paste a structure</h3>--}%
    %{--</div>--}%

    %{--<div class="modal-body">--}%
        %{--<g:render template="/chemAxon/marvinSketch"/>--}%
    %{--</div>--}%

    %{--<div class="modal-footer">--}%
        %{--<g:form name="structureSearchForm">--}%
            %{--<div class="control-group"><div class="controls">--}%
                %{--<g:radioGroup name="structureSearchType"--}%
                              %{--values="${StructureSearchParams.Type.values()}"--}%
                              %{--value="${StructureSearchParams.Type.Substructure}"--}%
                              %{--labels="${StructureSearchParams.Type.values()}">--}%
                    %{--<label class="radio inline">--}%
                        %{--${it.radio} ${it.label}--}%
                    %{--</label>--}%
                %{--</g:radioGroup>--}%
            %{--</div></div>--}%
            %{--<a href="#" class="btn" data-dismiss="modal" id="closeButton">Close</a>--}%
            %{--<a id="structureSearchButton" href="#" class="btn btn-primary" data-dismiss="modal">Search</a>--}%
        %{--</g:form>--}%
    %{--</div>--}%
%{--</div>--}%
