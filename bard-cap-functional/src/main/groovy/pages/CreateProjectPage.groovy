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

package pages

import geb.Page

/**
 * @author Muhammad.Rafique
 * Date Created: 2013/11/20
 */
class CreateProjectPage extends Page {
	static url = "project/create"
	static at = { title.contains("Create New Project") }

	static content = {
		nameField { $("#name") }
		descriptionField { $("#description") }
		projectStatus{ $("#projectStatus") }
		groupType{ $("#projectGroupType") }
		ownerRole { $("#ownerRole") }
		form { $("form") }
		cancelBtn { form.find("a.btn") }
		createBtn { form.find("input.btn.btn-primary", type:"submit") }
	}
	
	ViewProjectDefinitionPage CreateNewProject(def testData){
		nameField.value(testData.name)
		descriptionField.value(testData.description)
		projectStatus.value(testData.status)
		groupType.value(testData.group)
		ownerRole.value(testData.owner)
		createBtn.click()
		
		return new ViewProjectDefinitionPage()
	}
}
