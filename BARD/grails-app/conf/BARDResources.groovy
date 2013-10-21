modules = {
    overrides {
        'jquery-theme' {
            resource id: 'theme', url: '/css/flick/jquery-ui-1.8.20.custom.css'
        }
        'bootstrap' {
            resource id: 'bootstrap-css', url: '/js/jquery-ui-bootstrap/bootstrap/bootstrap.css'
        }
    }
    core {
        dependsOn 'jquery, jquery-ui, jquery-theme, jquery-validation-ui, dataTables, jqueryform'
        resource url: '/css/datatables/demo_table_jui.css'
        resource url: '/js/application.js'
        resource url: "css/bard.css"
        resource url: '/css/dl-horizontal-fix.css'
        resource url: '/js/persona/include.js'
        resource url: 'js/persona/signin.js'
    }
    accessontology {
        resource url: "/js/cap/accessOntology.js"
    }
    addAllItemsToCarts {
        resource url: "js/addAllItemsToCart.js"
    }
    assaycards {
        dependsOn('card')
        resource url: '/js/cap/assay.cards.js'
    }

    assaycompare {
        dependsOn 'core,bootstrap,bootstrapplus,card'
        resource url: '/js/cap/assaycompare.js'
    }
    assayshow {
        dependsOn 'bootstrapplus, card, sectionCounter'
        resource url: '/js/dynatree-1.2.2/jquery.dynatree.js'
        resource url: '/js/cap/assay.show.js'
        resource url: '/css/measures-dynatree.css'
    }
    assaysummary {
        resource url: '/js/cap/editSummary.js'
    }
    autocomplete {
        resource url: "js/autocomplete.js"
    }
    bardHeaderFooter {
        resource url: "css/bardHomepage/BardHeaderFooter.css"
    }
    bardHomepage {
        dependsOn 'core,bootstrap,bootstrap-responsive-css'
        resource url: '/css/bardHomepage/BardHomepage.css'
        resource url: '/css/bardHomepage/jquery-ui-1.10.3.custom.css'
        resource url: '/js/bardHomepage/jquery.main.js'
        resource url: '/js/errorReporting.js'
        resource url: '/js/idSearchDialog.js'
        resource url: '/js/bardHomepage/jquery-ui-1.10.3.custom.js'
        resource url: '/js/jquery-ui-extensions/autocomplete/jquery.ui.autocomplete.accentFolding.js'
        resource url: '/js/jquery-ui-extensions/autocomplete/jquery.ui.autocomplete.html.js'
    }

    basic {
        dependsOn 'core'
        resource url: "/css/layout.css"
        resource url: "/css/table.css"
        resource url: "/css/bardHomepage/BardHeaderFooter.css"
        resource url: '/js/jquery-ui-extensions/autocomplete/jquery.ui.autocomplete.accentFolding.js'
        resource url: '/js/jquery-ui-extensions/autocomplete/jquery.ui.autocomplete.html.js'
    }

    bootstrapplus {
        resource url: '/css/bootstrap-plus.css'
    }
    canEditWidget {
        resource url: "/css/caneditwidget.css"
    }
    card {
        resource url: '/css/card.css'
    }
    cardDisplayCSS {
        resource url: "css/card.css"
    }
    cart {
        resource url: "js/cart.js", disposition: 'head'
        resource url: "css/cart.css"
    }
    cbas {
        dependsOn 'bootstrap'
        resource url: "css/cbas.css"
    }

    compoundOptions {
        resource url: "js/compoundOptions.js"
    }

    contextItem {
        dependsOn 'select2, bootstrapplus, card'
        resource url: '/js/cap/contextItem.js'
        resource url: '/css/contextItem.css'
    }
    createProject {
        resource url: '/js/cap/createProject.js'
    }

    //D
    d3Library {
        resource url: "js/lib/d3.min.js"
    }
    dataTables {
        resource url: "js/DataTables-1.9.4/jquery.dataTables.js"
        resource url: "css/jquery-dataTables.css"
    }
    dateTimePicker {
        resource url: '/js/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js'
        resource url: '/js/bootstrap-datetimepicker/css/datetimepicker.css'
    }
    dcLibrary {
        resource url: "js/lib/dc.js"
        resource url: "js/lib/crossfilter.js"
        resource url: "css/dc.css"
    }
    dictionaryPage {
        dependsOn("tableSorter")
        resource url: "js/html5historyapi/history.js"
    }

    downtime {
        //dependsOn 'grailsEvents' //from the grails event push plugin
        resource url: '/js/cap/downtime.js'

    }
    dynatree {
        dependsOn 'jquery, jquery-ui'
        resource url: '/js/dynatree-1.2.2/jquery.dynatree.js'
        resource url: '/js/dynatree-1.2.2/skin/ui.dynatree.css'
    }

    //E
    experimentData {
        //Polyfill for handling History
        resource url: "js/html5historyapi/history.js"
        resource url: "js/experimentalResults.js"
    }
    experimentsummary {
        resource url: '/js/cap/editExperimentSummary.js'
    }
    //G
    grailspagination {
        resource url: '/css/grailspagination.css'
    }
    handlebars {
        resource url: "/js/handlebars-1.0.rc.2/handlebars.js"
    }
    //H
    histogram {
        dependsOn 'bootstrap,jquery,d3Library'

        resource url: "js/histogram/experimentalResultsHistogram.js"
        resource url: "css/experimentalResultHistogram.css"
    }

    //I
    idSearch {
        resource url: "js/idSearchDialog.js"
    }
    //J
    jqueryform {
        resource url: '/js/jquery.form.js'
    }
    jqueryMobile {
        dependsOn 'jqueryMobilePreInit'
        resource url: "jquery.mobile-1.3.1/jquery.mobile-1.3.1.css"
        resource url: "jquery.mobile-1.3.1/jquery.mobile.structure-1.3.1.css"
        resource url: "jquery.mobile-1.3.1/jquery.mobile.theme-1.3.1.css"
        resource url: "css/bard-mobile.css"
        resource url: "jquery.mobile-1.3.1/jquery.mobile-1.3.1.js"
        resource url: "js/jqueryMobilePostInit.js"
    }
    jqueryMobilePreInit {
        dependsOn 'jquery, jquery-ui, jquery-theme'
        resource url: "js/jqueryMobilePreInit.js"
    }
    jquerynotifier {
        dependsOn 'jquery, jquery-ui, jquery-theme'
        resource url: 'js/jquery-notifier/jquery.notify.js'
        resource url: 'js/jquery-notifier/ui.notify.css'
    }
    jsDrawEditor {
        resource url: "js/jsDraw/jsDrawEditor.js"
        resource url: "css/jsDrawEditor.css"
    }
    //L
    login {
        resource url: "css/font-awesome/css/font-awesome.css"
        resource url: "css/social-buttons.css"
    }
    //m
    molecularSpreadSheet {
        dependsOn "dataTables"
        resource url: "js/molecularSpreadSheet.js"
        resource url: "css/datatables_supplemental.css"
    }
    moveExperiments {
        dependsOn 'core,bootstrap'
        resource url: '/js/cap/moveExperiments.js'
    }

    newTerm {
        resource url: '/css/newterm/newTerms.css'
        resource url: '/js/element/newTerm.js'

    }

    projectstep {
        dependsOn "handlebars,zyngaScroller"
        resource url: '/js/projectstep/projectstep.edit.js'
        resource url: '/js/projectstep/projectstep.show.js'
        resource url: '/js/projectstep/viz.js'
        resource url: '/css/projectstep.css'
    }
    projectsummary {
        dependsOn('card')
        resource url: '/js/cap/editProjectSummary.js'
    }

    promiscuity {
        resource url: "js/promiscuity.js"
        resource url: "css/promiscuity.css"
    }
    richtexteditor {
    }
    richtexteditorForCreate {
        resource url: "/js/cap/createDocument.js"
    }
    richtexteditorForEdit {
        resource url: "/css/editDocument.css"
        resource url: "/js/cap/editDocument.js"
    }

//Adding version allows clients to not cache javascript
    search {
        resource url: "js/search.js"
        resource url: "css/facetDiv.css"
        resource url: "css/searchResults.css"
    }
    sectionCounter {
        resource url: '/css/sectioncounter.css'
    }
    select2 {
        dependsOn 'jquery'
        resource url: "/js/select2-3.4.3/select2.css"
        resource url: "/js/select2-3.4.3/select2.js"
    }
    showProjectAssay {
        dependsOn("cardDisplayCSS")
        //Polyfill for handling History
        resource url: "js/html5historyapi/history.js"
        resource url: "js/coreShowProjectAssay.js"
        // Stylesheet for context card view
    }
    structureSearch {
        resource url: "js/structureSearchDialog.js"
    }
    substances {
        resource url: "js/substances.js"
    }
    sunburst {
        dependsOn 'bootstrap,jquery,d3Library,dcLibrary'

        resource url: "js/sunburst/linkedVis.js"
        resource url: "js/sunburst/createALegend.js"
        resource url: "js/sunburst/linkedVisualizationModule.js"
        resource url: "js/sunburst/sharedStructures.js"
        resource url: "js/sunburst/createASunburst.js"
        resource url: "css/sunburst.css"
    }
    tableSorter {
        resource url: '/js/jquery-table-sorter/jquery.tablesorter.min.js'
        resource url: '/js/jquery-table-sorter/theme.default.css'
    }
    twitterBootstrapAffix {
        resource url: "/css/twitterBootstrapAffix.css"
        resource url: "/js/jquery-ui-bootstrap/bootstrap/js/twitterBootstrapAffix.js"
    }
    xeditable {
        resource url: "/js/x-editable/bootstrap-editable.js"
        resource url: "/css/x-editable/bootstrap-editable.css"
        resource url: "/js/x-editable/moment.js"
        resource url: "/js/x-editable/combodate.js"
    }

    zyngaScroller {
        resource url: "/js/zynga-scroller-a44d7c2/Animate.js"
        resource url: "/js/zynga-scroller-a44d7c2/Scroller.js"
        resource url: "/js/zynga-scroller-a44d7c2/render.js"
    }
}
