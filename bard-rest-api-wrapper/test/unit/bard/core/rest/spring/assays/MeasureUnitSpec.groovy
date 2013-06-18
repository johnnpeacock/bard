package bard.core.rest.spring.assays

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class MeasureUnitSpec extends Specification {
    @Shared
    ObjectMapper objectMapper = new ObjectMapper()

    public static final String MEASURE = '''
    {
        "id": 7186,
        "name": "Context for percent activity",
        "comps":
        [
            {
                "entityId": null,
                "entity": "assay",
                "source": "cap-context",
                "id": 7186,
                "display": ".05 um",
                "contextRef": "Context for percent activity",
                "key": "screening concentration",
                "value": null,
                "extValueId": null,
                "url": null,
                "displayOrder": 0,
                "related": "measureRefs:22510"
            }
        ]
    }
       '''

    void "test serialization to Context"() {
        when:
        final Measure measure = objectMapper.readValue(MEASURE, Measure.class)
        then:
        assert measure.id==7186
        assert measure.name== "Context for percent activity"
        List<Annotation> comps = measure.comps
        assert comps
        assert comps.size() == 1
        Annotation comp = comps.get(0)
        assert comp.display==".05 um"
        assert comp.entity=="assay"
        assert !comp.entityId
        assert comp.source== "cap-context"
        assert comp.id==7186
        assert comp.contextRef=="Context for percent activity"
        assert comp.key== "screening concentration"
        assert !comp.value
        assert !comp.extValueId
        assert !comp.url
        assert comp.displayOrder==0
        assert comp.related=="measureRefs:22510"
    }


}

