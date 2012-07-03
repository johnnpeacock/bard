package dataexport.util

import common.tests.XmlTestAssertions
import common.tests.XmlTestSamples
import grails.plugin.spock.IntegrationSpec
import groovy.xml.MarkupBuilder
import org.custommonkey.xmlunit.XMLAssert
import spock.lang.Unroll

@Unroll
class RootServiceIntegrationSpec extends IntegrationSpec {
    RootService rootService
    Writer writer
    MarkupBuilder markupBuilder


    void setup() {
        this.writer = new StringWriter()
        this.markupBuilder = new MarkupBuilder(this.writer)
    }


    void "test generate root links"() {
        when:
        rootService.generateRootElement(this.markupBuilder)
        then:
        XmlTestAssertions.assertResults(results, writer.toString())
        //assert that there are two links, one each for dictionary and assays
        XMLAssert.assertXpathEvaluatesTo("3", "count(//link)", writer.toString());
        XMLAssert.assertXpathEvaluatesTo("1", "count(//link[@type='application/vnd.bard.cap+xml;type=assays'])", writer.toString())
        XMLAssert.assertXpathEvaluatesTo("1", "count(//link[@type='application/vnd.bard.cap+xml;type=dictionary'])", writer.toString())
        XMLAssert.assertXpathEvaluatesTo("1", "count(//link[@type='application/vnd.bard.cap+xml;type=projects'])", writer.toString())
        XMLAssert.assertXpathEvaluatesTo("0", "count(//link[@type='application/vnd.bard.cap+xml;type=experiments'])", writer.toString())

        where:
        label                 | results
        "Dictionary Root Url" | XmlTestSamples.BARD_DATA_EXPORT
    }
}