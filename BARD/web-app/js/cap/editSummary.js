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

$(document).ready(function () {

    //inline editing
    //$.fn.editable.defaults.mode = 'inline';

    //Set up editing for button
    $('.documentPencil').click(function (e) {
        e.stopPropagation();
        e.preventDefault();
        var dataId = $(this).attr('data-id');
         $("#" + dataId).editable('toggle');
    });

    //edit status
    $('.status').editable({
        mode:'inline',
        params: function (params) {
            params.version = $('#versionId').val();
            return params;
        },
        validate: function (value) {
            return validateInput(value);
        },
        success: function (response, newValue) {
            updateSummary(response, newValue);
        }
    });

    //edit  type
    $('.type').editable({
        mode:'inline',
        params: function (params) {
            params.version = $('#versionId').val();
            return params;
        },
        validate: function (value) {
            return validateInput(value);
        },
        success: function (response, newValue) {
            updateSummary(response, newValue);
        }
    });

    //edit name
    $('#nameId').editable({
        mode:'inline',
        inputclass: 'input-large',
        params: function (params) {
            params.version = $('#versionId').val();
            return params;
        },
        validate: function (value) {
            return validateInput(value);
        },
        success: function (response, newValue) {
            updateSummary(response, newValue);
        }
    });

    //edit designed by
    $('.designedBy').editable({
        mode:'inline',
        inputclass: 'input-large',
        params: function (params) {
            params.version = $('#versionId').val();
            return params;
        },
        validate: function (value) {
            return validateInput(value);
        },
        success: function (response, newValue) {
            updateSummary(response, newValue);
        }
    });
});
function updateSummary(response, newValue) {

    var version = response.version;
    var modifiedBy = response.modifiedBy;
    var lastUpdated = response.lastUpdate;
    var shortName = response.shortName;


    $("#versionId").val(version);
    $("#modifiedById").html(modifiedBy);
    $("#lastUpdatedId").html(lastUpdated);
    $("#shortNameId").html(shortName);
    return response.data;
}

function validateInput(value) {
    if ($.trim(value) == '') {
        return 'Required and cannot be empty';
    }
}


