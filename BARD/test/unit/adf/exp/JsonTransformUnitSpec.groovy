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

package adf.exp

import adf.exp.AbstractResultTree
import adf.exp.Box
import adf.exp.JsonTransform
import adf.exp.Path
import adf.exp.ResultKey
import au.com.bytecode.opencsv.CSVReader
import bard.db.experiment.JsonResult
import bard.db.experiment.JsonSubstanceResults
import spock.lang.Specification

/**
 * Created with IntelliJ IDEA.
 * User: pmontgom
 * Date: 4/9/14
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
class JsonTransformUnitSpec extends Specification {
    def transformToJson(def pair) {
        def resultType, children;
        resultType = pair[0];
        if (pair.size() == 2) {
            children = pair[1];
        }
        return new JsonResult(contextItems: [], resultTypeId: resultType, resultType: resultType.toString(), related: children.collect { transformToJson(it) }, valueDisplay: "${resultType*10}" )
    }

    def makeJsonResults(def roots) {
        return new JsonSubstanceResults(sid: 0, rootElem: roots.collect { transformToJson(it) })
    }

    def testWriteTwoBoxes() {
        setup:
        def results = makeJsonResults([[1, [[2, []]]]])
        Map<Path, Box> boxByPath = [:]

        JsonResult jr1 = new JsonResult(resultType: "1", resultTypeId: 1)
        JsonResult jr2 = new JsonResult(resultType: "2", resultTypeId: 2)

        Path path1 = new Path(jr1, null)
        Path path2 = new Path(jr2, path1)
        boxByPath[null] = new Box([new ResultKey(jr1)], [])
        boxByPath[path2] = new Box([new ResultKey(jr2)], [])

        when:
        JsonTransform.BoxesWriter writer = new JsonTransform.BoxesWriter("out/dataset_",boxByPath)
        writer.write(results)
        writer.close()

        CSVReader reader = new CSVReader(new FileReader("out/dataset_1.txt"))
        String[] header = reader.readNext()
        String [] data = reader.readNext()

        then:
        reader.readNext() == null

        header as List == ["sid", "resultId", "parentResultId", "1"]
        data as List == ["0", "1", "", "10"]

        when:
        reader.close();
        reader = new CSVReader(new FileReader("out/dataset_2.txt"))
        header = reader.readNext()
        data = reader.readNext()

        then:
        reader.readNext() == null

        header as List == ["sid", "resultId", "parentResultId", "2"]
        data as List == ["0", "2", "1", "20"]


    }

    def testWriteSingleBox() {
        setup:
        def results = makeJsonResults([[1, [[2, []]]]])
        Map<Path, Box> boxByPath = [:]

        JsonResult jr1 = new JsonResult(resultType: "1", resultTypeId: 1)
        JsonResult jr2 = new JsonResult(resultType: "2", resultTypeId: 2)

//        Path path1 = new Path(jr1, null)
//        Path path2 = new Path(jr2, path1)
        boxByPath[null] = new Box([new ResultKey(jr1), new ResultKey(jr2)], [])

        when:
        JsonTransform.BoxesWriter writer = new JsonTransform.BoxesWriter("out/dataset_",boxByPath)
        writer.write(results)
        writer.close()

        CSVReader reader = new CSVReader(new FileReader("out/dataset_1.txt"))
        String[] header = reader.readNext()
        String [] data = reader.readNext()

        then:
        reader.readNext() == null

        header as List == ["sid", "resultId", "parentResultId", "1", "2"]
        data as List == ["0", "1", "", "10", "20"]
    }

    def testSimpleBoxing() {
        setup:
        def results = makeJsonResults([[1, [[2, []]]]])

        JsonTransform transform = new JsonTransform()
        AbstractResultTree tree = transform.constructTree([results]);

        when:
        Map<Path, Box> boxByPath = tree.constructBoxes()

        then:
        boxByPath.size() == 1
        Box rootBox = boxByPath[null]
        rootBox.columns.size() == 2
    }

    def testTwoBoxes() {
        setup:
        def results = makeJsonResults([[1, [[2,[]], [2, []]]]])

        JsonTransform transform = new JsonTransform()
        AbstractResultTree tree = transform.constructTree([results]);

        JsonResult rt1 = new JsonResult(resultType: "1", resultTypeId: 1)
        JsonResult rt2 = new JsonResult(resultType: "2", resultTypeId: 2)
        Path path1 = new Path(rt1, null)
        Path path2 = new Path(rt2, path1)

        when:
        Map<Path, Box> boxByPath = tree.constructBoxes()

        then:
        boxByPath.size() == 2
        Box rootBox = boxByPath[null]
        rootBox.columns.size() == 1

        Box secondBox = boxByPath[path2]
        secondBox.columns.size() == 1
    }


    def testSingleChild() {
        setup:
        JsonTransform transform = new JsonTransform()

        def results = makeJsonResults([[1, [[2, []]]]])

        when:
        AbstractResultTree tree = transform.constructTree([results]);

        then:
        AbstractResultTree.Node node = tree.pathNodeMap[null]
        node.children.size() == 1

        when:
        AbstractResultTree.Node parent = node.children.first()

        then:
        parent.children.size() == 1

        when:
        AbstractResultTree.Node child = parent.children.first()

        then:
        child.children.size() == 0
        child.groupWithParent
        child.path.parent.parent == null
        child.path.result.resultTypeId == 2
        tree.pathNodeMap.size() == 3
    }

    def testMultipleChild() {
        setup:
        JsonTransform transform = new JsonTransform()
        def results = new JsonSubstanceResults(sid: 0, rootElem:  [ new JsonResult(contextItems: [], resultTypeId: "1", resultType: "parent", related:
                [
                        new JsonResult(contextItems: [], resultTypeId: "2", resultType: "child", related:[]),
                        new JsonResult(contextItems: [], resultTypeId: "2", resultType: "child", related:[]),
                ]
        ) ] );

        when:
        AbstractResultTree tree = transform.constructTree([results]);

        then:
        AbstractResultTree.Node node = tree.pathNodeMap[null]
        node.children.size() == 1

        when:
        AbstractResultTree.Node parent = node.children.first()

        then:
        parent.children.size() == 1

        when:
        AbstractResultTree.Node child = parent.children.first()

        then:
        !child.groupWithParent
        child.children.size() == 0
        child.path.parent.parent == null
        child.path.result.resultTypeId == "2"
        tree.pathNodeMap.size() == 3
    }
}
