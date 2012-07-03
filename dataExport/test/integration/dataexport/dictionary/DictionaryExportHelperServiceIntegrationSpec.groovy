package dataexport.dictionary

import bard.db.dictionary.Element
import bard.db.dictionary.ResultType
import bard.db.dictionary.Stage
import common.tests.XmlTestAssertions
import common.tests.XmlTestSamples
import grails.plugin.spock.IntegrationSpec
import groovy.xml.MarkupBuilder

class DictionaryExportHelperServiceIntegrationSpec extends IntegrationSpec {
    DictionaryExportHelperService dictionaryExportHelperService
    Writer writer
    MarkupBuilder markupBuilder

    void setup() {
        this.writer = new StringWriter()
        this.markupBuilder = new MarkupBuilder(this.writer)

    }

    void tearDown() {
        // Tear down logic here
    }

    void "test generate units #label"() {
        when:
        this.dictionaryExportHelperService.generateUnits(this.markupBuilder)
        then:
        XmlTestAssertions.assertResults(results, this.writer.toString())
        where:
        label       | results
        "All Units" | XmlTestSamples.UNITS
    }

    void "test generate Unit Conversions #label"() {
        when:
        this.dictionaryExportHelperService.generateUnitConversions(this.markupBuilder)
        then:
        XmlTestAssertions.assertResults(results, this.writer.toString())
        where:
        label                  | results
        "All Unit Conversions" | XmlTestSamples.UNIT_CONVERSIONS

    }

    void "test generate Result Types"() {
        when:
        this.dictionaryExportHelperService.generateResultTypes(this.markupBuilder)
        then:
        println this.writer.toString()
        XmlTestAssertions.assertResults(results, this.writer.toString())
        where:
        label          | results
        "Result Types" | XmlTestSamples.RESULT_TYPES

    }

    void "test generate Result Type"() {
        given:
        final Element element = Element.get(new BigDecimal("341"))
        final ResultType resultType = ResultType.findByElement(element)
        when:
        this.dictionaryExportHelperService.generateResultType(this.markupBuilder, resultType)
        then:
        println this.writer.toString()
        XmlTestAssertions.assertResults(results, this.writer.toString())
        where:
        label         | results
        "Result Type" | XmlTestSamples.RESULT_TYPE

    }

    void "test generate Element hierarchies #label"() {
        when:
        this.dictionaryExportHelperService.generateElementHierarchies(this.markupBuilder)
        then:
        println this.writer.toString()
        XmlTestAssertions.assertResults(results, this.writer.toString())
        where:
        label         | results
        "Hierarchies" | XmlTestSamples.ELEMENT_HIERARCHIES

    }

    void "test generate element with id"() {
        given:
        final Element element = Element.get(new BigDecimal("386"))
        when:
        this.dictionaryExportHelperService.generateElement(this.markupBuilder, element)
        then:
        XmlTestAssertions.assertResultsWithOverrideAttributes(results, this.writer.toString())
        where:
        label      | results
        "Elements" | XmlTestSamples.ELEMENT
    }

    void "test generate elements #label"() {
        when:
        this.dictionaryExportHelperService.generateElements(this.markupBuilder)
        then:
        XmlTestAssertions.assertResults(results, this.writer.toString())
        where:
        label      | results
        "Elements" | XmlTestSamples.ELEMENTS

    }

    void "test generate Stages"() {
        when:
        this.dictionaryExportHelperService.generateStages(this.markupBuilder)
        then:
        println this.writer.toString()
        XmlTestAssertions.assertResultsWithOverrideAttributes(results, this.writer.toString())
        where:
        label    | results
        "Stages" | XmlTestSamples.STAGES

    }

    void "test generate Stage"() {
        given:
        final Element element = Element.get(new BigDecimal("341"))
        final Stage stage = Stage.findByElement(element)
        when:
        this.dictionaryExportHelperService.generateStage(this.markupBuilder, stage)
        then:
        println this.writer.toString()
        XmlTestAssertions.assertResults(results, this.writer.toString())
        where:
        label   | results
        "Stage" | XmlTestSamples.STAGE

    }

    void "test generate Labs"() {
        when:
        this.dictionaryExportHelperService.generateLabs(this.markupBuilder)
        then:
        XmlTestAssertions.assertResults(results, this.writer.toString())
        where:
        label  | results
        "Labs" | XmlTestSamples.LABS


    }

    void "test generate dictionary #label"() {
        when:
        this.dictionaryExportHelperService.generateDictionary(this.markupBuilder)
        then:
        println this.writer.toString()
        XmlTestAssertions.assertResultsWithOverrideAttributes(results, this.writer.toString())
        where:
        label        | results
        "Dictionary" | XmlTestSamples.DICTIONARY

    }

}