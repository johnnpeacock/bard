package bard.db.experiment

import au.com.bytecode.opencsv.CSVReader
import bard.db.dictionary.Element
import bard.db.experiment.Experiment
import bard.db.experiment.ExperimentContext
import bard.db.experiment.ExperimentContextItem
import bard.db.experiment.ExperimentMeasure
import bard.db.experiment.HierarchyType
import bard.db.experiment.Result
import bard.db.experiment.ResultContextItem
import bard.db.experiment.ResultHierarchy
import bard.db.experiment.Substance
import bard.db.registration.Assay
import bard.db.registration.AssayContext
import bard.db.registration.AssayContextItem
import bard.db.registration.AssayContextMeasure
import bard.db.registration.AttributeType
import bard.db.registration.ItemService
import bard.db.registration.Measure
import bard.db.registration.PugService
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.commons.GrailsApplication

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class ResultsService {

    // the number of lines to show the user after upload completes
    static int LINES_TO_SHOW_USER = 10;

    static String NUMBER_PATTERN_STRING = "[+-]?[0-9]+(\\.[0-9]*)?([Ee][+-]?[0-9]+)?"

    // pattern matching a number
    static Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_PATTERN_STRING)

    static String QUALIFIER_PATTERN_STRING = (Result.QUALIFIER_VALUES.collect{ "(?:${it.trim()})"}).join("|")

    // pattern matching a qualifier followed by a number
    static Pattern QUALIFIED_NUMBER_PATTERN = Pattern.compile("(${QUALIFIER_PATTERN_STRING})?\\s*(${NUMBER_PATTERN_STRING})")

    // pattern matching a range of numbers.  Doesn't actually check that the two parts are numbers
    static Pattern RANGE_PATTERN = Pattern.compile("([^-]+)-(.*)")

    static String EXPERIMENT_ID_LABEL = "Experiment ID"
    static String EXPERIMENT_NAME_LABEL = "Experiment Name"
    static List FIXED_COLUMNS = ["Row #", "Substance", "Replicate #", "Parent Row #"]
    static int MAX_ERROR_COUNT = 100;

    ItemService itemService
    PugService pugService
    ResultsExportService resultsExportService
    ArchivePathService archivePathService

    static boolean isNumber(value) {
        return NUMBER_PATTERN.matcher(value).matches()
    }

    static def parseListValue(String value, List<AssayContextItem> contextItems) {
        if (isNumber(value)) {
            float v = Float.parseFloat(value)
            float smallestDelta = Float.MAX_VALUE
            float closestValue = Float.NaN

            contextItems.each {
                def delta = Math.abs(it.valueNum - v)
                if (delta < smallestDelta) {
                    smallestDelta = delta
                    closestValue = it.valueNum
                }
            }
            return new Cell(value: closestValue, qualifier: "= ", valueDisplay: closestValue.toString())
        } else {
            def labelMap = [:]
            contextItems.each {
                if(it.valueDisplay != null)
                    labelMap[it.valueDisplay.trim()] = it
            }
            AssayContextItem selectedItem = labelMap[value.trim()]
            if (selectedItem == null) {
                return "Could not find \"${value}\" among values in list: ${labelMap.keySet()}"
            }
            return new Cell(element: selectedItem.valueElement, valueDisplay: selectedItem.valueDisplay)
        }
    }

    static def parseQualifiedNumber(String value) {
        Matcher matcher = QUALIFIED_NUMBER_PATTERN.matcher(value)

        if (matcher.matches()) {
            String foundQualifier = matcher.group(1)
            if (foundQualifier == null) {
                foundQualifier = "= "
            }

            if (foundQualifier.length() < 2) {
                foundQualifier += " "
            }

            float a
            try
            {
                a = Float.parseFloat(matcher.group(2));
            }
            catch(NumberFormatException e)
            {
                return "Could not parse \"${matcher.group(2)}\" as a number"
            }

            String valueDisplay = a.toString()
            if (foundQualifier != "= ") {
                valueDisplay = foundQualifier.trim()+valueDisplay
            }

            Cell cell = new Cell(value: a, qualifier: foundQualifier, valueDisplay: valueDisplay )

            return cell
        } else {
            return "Could not parse \"${value}\" as a number with optional qualifier"
        }
    }

    static def parseRange(String value) {
        def rangeMatch = RANGE_PATTERN.matcher(value)
        if (rangeMatch.matches()) {
            float minValue, maxValue
            try
            {
                minValue = Float.parseFloat(rangeMatch.group(1));
                maxValue = Float.parseFloat(rangeMatch.group(2));
            }
            catch(NumberFormatException e)
            {
                return "Could not parse \"${value}\" as a range"
            }

            Cell cell = new Cell(minValue: minValue, maxValue: maxValue, valueDisplay: "${minValue}-${maxValue}")
            return cell
        }

        return "Expected a range, but got \"${value}\""
    }

    static def parseAnything(String value) {
        if (RANGE_PATTERN.matcher(value).matches()) {
            return parseRange(value)
        } else if(QUALIFIED_NUMBER_PATTERN.matcher(value).matches()) {
            return parseQualifiedNumber(value)
        } else {
            // assume it's free text and we take it literally
            return new Cell(valueDisplay:value)
        }
    }

    static def parseNumberOrRange(String value) {
        if (RANGE_PATTERN.matcher(value).matches()) {
            return parseRange(value)
        } else {
            return parseQualifiedNumber(value)
        }
    }

    /*
    static class Column {
        String name;

        // if this column represents a measurement
        Measure measure;

        // if this column represents a context item
        ItemService.Item item;

        Closure parser;

        // return a string if error.  Otherwise returns a Cell
        def parseValue(String value) {
            return parser(this, value)
        }

        public Column(String name, Measure measure) {
            this.name = name
            this.measure = measure
            this.parser = { Column column, String value -> parseAnything(column, value) }
        }

        public Column(String name, ItemService.Item item) {
            this.item = item;
            this.name = name;
            this.parser = makeItemParser(item)
        }

        public String toString() {
            return "${name}"
        }
    }
    */

    static class Row {
        int lineNumber;

        Integer rowNumber;
        Integer replicate;
        Integer parentRowNumber;
        Long sid;

        List<RawCell> cells = [];
    }

    static class Cell {
        String qualifier;
        Float value;

        Float minValue;
        Float maxValue;

        Element element;

        String valueDisplay;

        /*
        String getValueDisplay() {
            if (valueDisplay != null) {
                return valueDisplay;
            } else {
                String valueString = null;

                if (value != null) {
                    if (qualifier == "= " ) {
                        valueString = value.toString()
                    } else {
                        valueString = "${qualifier.trim()}${value}"
                    }
                }

                if (minValue != null) {
                    valueString = "${minValue}-${maxValue}";
                }

                if (element != null) {
                    valueString element.label
                }

                // now try to find the units
                String unit = null;
                if (column.item != null) {
                    unit = column.item.attributeElement.unit?.abbreviation
                } else if (column.measure != null) {
                    unit = column.measure.resultType?.unit?.abbreviation;
                }
                if (unit != null) {
                    valueString += " ${unit}"
                }
                return valueString;
            }
        }

        String toString() {
            "Cell(${column})"
        }
        */
    }

    static class RawCell {
        String columnName;
        String value;
    }

    static class ImportSummary {
        def errors = []

        // these are just collected for purposes of reporting the import summary at the end
        int linesParsed = 0;
        int resultsCreated = 0;
        int experimentAnnotationsCreated = 0;
        Map<String, Integer> resultsPerLabel = [:]
        Set<Long> substanceIds = [] as Set

        int resultsWithRelationships = 0;
        int resultAnnotations = 0;

        List<List> topLines = []

        public int getSubstanceCount() {
            return substanceIds.size()
        }

        void addError(int line, int column, String message) {
            if (!tooMany()) {
                if (line != 0) {
                    errors << "On line ${line}, column ${column+1}: ${message}"
                } else {
                    errors << message
                }
            }
        }

        boolean hasErrors() {
            return errors.size() > 0
        }

        boolean tooMany() {
            return errors.size() > MAX_ERROR_COUNT;
        }
    }

    static class Template {
        Experiment experiment;
        List<String> constantItems;
        List<String> columns;

        List asTable() {
            def lines = []

            lines.add(["",EXPERIMENT_ID_LABEL, experiment.id])
            lines.add(["",EXPERIMENT_NAME_LABEL, experiment.experimentName])

            // add the fields for values that are constant across entire experiment
            constantItems.each { lines.add(["",it]) }
            lines.add([])

            // add the first line of the header
            def row = []
            row.addAll(FIXED_COLUMNS)
            columns.each {row.add(it)}
            lines.add(row)
            lines.add(["1"])

            return lines
        }
    }

    List generateMaxSchemaComponents(Experiment experiment) {
        def assay = experiment.assay

        def assayItems = assay.assayContextItems.findAll { it.attributeType != AttributeType.Fixed }
        def measureItems = assayItems.findAll { it.assayContext.assayContextMeasures.size() > 0 }
        assayItems.removeAll(measureItems)

        return [itemService.getLogicalItems(assayItems), experiment.experimentMeasures.collect {it.measure} as List, itemService.getLogicalItems(measureItems)]
    }

    Template generateMaxSchema(Experiment experiment) {
        def (experimentItems, measures, measureItems) = generateMaxSchemaComponents(experiment)
        return generateSchema(experiment, experimentItems, measures, measureItems)
    }

    /**
     * Construct list of columns that a result upload could possibly contain
     */
    Template generateSchema(Experiment experiment, List<ItemService.Item> constantItems, List<Measure> measures, List<ItemService.Item> measureItems) {
        Set<String> constants = [] as Set
        Set<String> columns = [] as Set

        // add all the non-fixed context items
        for(item in constantItems) {
            String name = item.attributeElement.label
            constants.add(name)
        }

        // add all of the measurements
        for(measure in measures) {
            String name = measure.displayLabel
            columns.add(name)
        }

        // add all the measure context items
        for(item in measureItems) {
            String name = item.attributeElement.label
            columns.add(name)
        }

        return new Template(experiment: experiment, constantItems: constants as List, columns: columns as List)
    }

    public static class InitialParse {
        String experimentName;
        Long experimentId;
        List<ExperimentContext> contexts = []
        int linesParsed;
        List<Row> rows;
        List<List<String>> topLines;
    }

    static class LineReader {
        CSVReader reader;
        int lineNumber = 0;

        List<List<String>> topLines = []

        String [] readLine() {
            lineNumber ++;
            String [] line = reader.readNext()

            if (line != null && topLines.size() < LINES_TO_SHOW_USER)
                topLines.add(line)

            return line;
        }

        public LineReader(BufferedReader reader) {
            this.reader = new CSVReader(reader);
        }
    }

    boolean allEmptyColumns(String[] columns) {
        for(column in columns) {
            if (!column.isEmpty())
                return false
        }
        return true
    }


    InitialParse parseConstantRegion(LineReader reader, ImportSummary errors, Collection<ItemService.Item> constantItems) {
        InitialParse result = new InitialParse()
        Map experimentAnnotations = [:]

        while(true) {
            String[] values = reader.readLine();
            if (values == null)
                break;

            // initial header stops on first empty line
            if (allEmptyColumns(values)) {
                break;
            }

            for(int i=3;i<values.length;i++) {
                if (!values[i].isEmpty()) {
                    errors.addError(reader.lineNumber, values.length, "Wrong number of columns in initial header.  Expected 3 but found value in column ${values[i]}")
                }
                continue
            }

            if (!values[0].isEmpty()) {
                errors.addError(reader.lineNumber, 0, "First column should be empty in the constant section at top of table")
                continue
            }

            if (values.length == 2 || values[2].isEmpty()) {
                // we have no value for the given key
                continue
            }

            String value = values[2]
            String key = values[1]
            if (key == EXPERIMENT_ID_LABEL) {
                result.experimentId = Long.parseLong(value)
            } else if (key == EXPERIMENT_NAME_LABEL) {
                result.experimentName = value
            } else {
                experimentAnnotations.put(key, value)
            }
        }

        Set<String> unusedKeyNames = new HashSet(experimentAnnotations.keySet())

        // walk through all the context items on the assay
        List<ExperimentContext> experimentContexts = []
        for(values in (constantItems.groupBy {it.assayContext}).values()) {
            ExperimentContext context = new ExperimentContext()
            for(item in values) {
                String label = item.displayLabel;

                unusedKeyNames.remove(label);
                String stringValue = experimentAnnotations.get(label)
                if (stringValue == null || stringValue.isEmpty())
                    continue;

                ExperimentContextItem experimentContextItem = createExperimentContextItem(item, stringValue, errors)
                if (experimentContextItem != null) {
                    context.contextItems.add(experimentContextItem)
                    experimentContextItem.experimentContext = context
                }
            }

            if (context.experimentContextItems.size() > 0)
                result.contexts.add(context)
        }

        for(unusedKey in unusedKeyNames) {
            errors.addError(0, 0, "Unknown field \"${unusedKey}\" in header")
        }

        return result
    }

    void forEachDataRow(LineReader reader, List<String> columns, ImportSummary errors, Closure fn) {
        int expectedColumnCount = columns.size() + FIXED_COLUMNS.size();

        while(true) {
            List<String> values = reader.readLine();
            if (values == null)
                break;

            // verify and reshape columns
            while(values.size() < expectedColumnCount) {
                values.add("")
            }

            // verify there aren't too many columns
            while(values.size() > expectedColumnCount) {
                String value = values.remove(values.size()-1)
                if (value.trim().length() != 0) {
                    errors.addError(reader.lineNumber, values.size()+1, "Found \"${value}\" in extra column")
                }
            }

            // now that values is guaranteed to be the right length, make the entire row isn't empty
            boolean allEmpty = true;
            for(cell in values) {
                if (!cell.isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }

            // pass to the callback
            if (!allEmpty)
                fn(reader.lineNumber, values)

            if (errors.tooMany())
                break
        }
    }

    Object[] safeParse(ImportSummary errors, List<String> values, int lineNumber, List<Closure> fns) {
        boolean hadFailure = false;

        Object[] parsed = new Object[fns.size()]
        for(int i=0;i<fns.size();i++) {
            try {
                parsed[i] = fns[i](values[i])
            } catch(Exception ex) {
                errors.addError(lineNumber, i, "Could not parse \"${values[i]}\"")
                hadFailure = true
//                ex.printStackTrace()
            }
        }

        if (hadFailure) {
            return null;
        } else {
            return parsed;
        }
    }

    List<String> parseTableHeader(LineReader reader, Template template, ImportSummary errors)      {
        List<String> columnNames = reader.readLine()

        // validate the fixed columns are where they should be
        for(int i = 0;i<FIXED_COLUMNS.size();i++) {
            if (columnNames.size() < i || columnNames[i] != FIXED_COLUMNS[i]) {
                errors.addError(reader.lineNumber, i, "Expected "+FIXED_COLUMNS[i]+" in column header at position "+(i+1))
            }
        }

        if (errors.hasErrors())
            return null

        def seenColumns = [] as Set

        def columns = []
        for(int i=FIXED_COLUMNS.size();i<columnNames.size();i++) {
            def name = columnNames[i]

            if (seenColumns.contains(name))
            {
                errors.addError(reader.lineNumber, i, "Duplicated column name \"${name}\"")
                columns.add("")
                continue
            }

            seenColumns.add(name)

            if (!template.columns.contains(name)) {
                errors.addError(reader.lineNumber, i, "Invalid column name \"${name}\"")
                columns.add("")
                continue;
            }

            columns.add(name)
        }

        return columns
    }

    Collection<Result> createResults(List<Row> rows, Collection<ExperimentMeasure> experimentMeasures, ImportSummary errors, Map<Measure, Collection<ItemService.Item>> itemsByMeasure) {
        validateParentRowsExist(rows, errors);
        if (errors.hasErrors())
            return []

        Map<Integer, Collection<Row>> byParent = rows.groupBy { it.parentRowNumber }

        // create a set which we'll use to track which cells were used at least once.  Since we're driven by
        // walking the measure tree, it's possible that some cells might not get consumed.  Those should be
        // errors.
        IdentityHashMap<RawCell, Row> unused = new IdentityHashMap();
        for(row in rows) {
            for(cell in row.cells) {
                unused.put(cell, row)
            }
        }

        // we're going to walk through the measure tree, taking items from the results now
        // that they've also been mapped into a tree

        // start with the rows with no parents because these must contain the root measures
        Collection<ExperimentMeasure> rootMeasures = experimentMeasures.findAll { it.parent == null }
        List<Result> results = []
        for(measure in rootMeasures) {
            results.addAll(extractResultFromEachRow(measure, byParent.get(null), byParent, unused, errors, itemsByMeasure))
        }

        return results
    }

    Collection<Result> extractResultFromEachRow(ExperimentMeasure measure, Collection<Row> rows, Map<Integer, Collection<Row>> byParent, IdentityHashMap<RawCell, Row> unused, ImportSummary errors, Map<Measure, ItemService.Item> itemsByMeasure) {
        List<Result> results = []

        for(row in rows) {
            Substance substance = Substance.get(row.sid)
            if(substance == null) {
                errors.addError(row.lineNumber, 0, "While creating results, could not find substance with id ${row.sid}")
                continue
            }

            Map<String, RawCell> valueByColumn = row.cells.collectEntries { [it.columnName, it] }

            String label = measure.measure.displayLabel
            RawCell cell = valueByColumn.get(label)

            if(cell != null) {
                // mark this cell as having been consumed
                unused.remove(cell)

                Result result = createResult(row.replicate, measure.measure, cell.value, row.sid, errors)
                if (result == null)
                    continue;

                // children can be on the same row or any row that has this row as its parent
                // so combine those two collections
                List<Row> possibleChildRows = [row]
                Collection<Row> childRows = byParent[row.rowNumber]
                if (childRows != null) {
                    possibleChildRows.addAll(childRows)
                }

                // for each child measure, create a result per row in each of the child rows
                for(child in measure.childMeasures) {
                    Collection<Result> resultChildren = extractResultFromEachRow(child, possibleChildRows, byParent, unused, errors, itemsByMeasure)

                    for(childResult in resultChildren) {
                        linkResults(child.parentChildRelationship, errors, 0, childResult, result);
                    }

                    results.addAll(resultChildren)
                }

                // likewise create each of the context items associated with this measure
                for(item in itemsByMeasure[measure.measure]) {
                    RawCell itemCell = valueByColumn[item.displayLabel]
                    if (itemCell != null) {
                        ResultContextItem resultItem = createResultItem(itemCell.value, item, errors)

                        if (resultItem != null) {
                            resultItem.result = result
                            result.resultContextItems.add(resultItem)
                        }
                    }
                }

                results.add(result)
            }
        }

        return results;
    }

    void validateParentRowsExist(Collection<Row> rows, ImportSummary errors) {
        def rowByNumber = [:]
        for(row in rows) {
            rowByNumber[row.rowNumber] = row
        }

        for(row in rows) {
            if (row.parentRowNumber != null && !rowByNumber.containsKey(row.parentRowNumber)) {
                errors.addError(row.lineNumber, 0, "Could not find row ${row.parentRowNumber} but this row ${row.rowNumber} is a child")
            }
        }
    }

    def parseContextItem(String stringValue, ItemService.Item item) {
        if (item.type == AttributeType.List) {
            return parseListValue(stringValue, item.contextItems)
        } else if (item.type == AttributeType.Free) {
            return parseAnything(stringValue)
        } else if (item.type == AttributeType.Range) {
            Double rangeMin = item.contextItems[0].valueMin
            Double rangeMax = item.contextItems[0].valueMax
            Double rangeName = item.attributeElement.label

            float floatValue = Float.parseFloat(stringValue)

            if (floatValue < rangeMin || floatValue > rangeMax) {
                return "The value \"${floatValue}\" outside of allowed range (${rangeMin} - ${rangeMax}) for ${rangeName}"
            }

            return new Cell(value: floatValue)
        } else {
            throw new RuntimeException("Did not know how to handle attribute type "+item.type)
        }
    }

    ResultContextItem createResultItem(String stringValue, ItemService.Item assayItem, ImportSummary errors) {
        def parsed = parseContextItem(stringValue, assayItem)

        if (parsed instanceof Cell) {
            Element unit = assayItem.attributeElement.unit;
            Cell cell = parsed
            ResultContextItem item = new ResultContextItem()

            item.attributeElement = assayItem.attributeElement
            item.valueNum= cell.value
            item.qualifier= cell.qualifier
            item.valueMin= cell.minValue
            item.valueMax= cell.maxValue
            item.valueElement = cell.element
            item.valueDisplay= cell.valueDisplay + (unit == null ? "" : " ${unit.abbreviation}")

            return item
        } else {
            errors.addError(0, 0, parsed)
            return null
        }
    }

    Result createResult(Integer replicate, Measure measure, String valueString, Long substanceId, ImportSummary errors) {
        def parsed = parseAnything(valueString)

        if (parsed instanceof Cell) {
            Cell cell = parsed
            Element unit = measure.resultType.unit;

            Result result = new Result()
            result.qualifier = cell.qualifier
            result.valueDisplay= cell.valueDisplay + (unit == null ? "" : " ${unit.abbreviation}")
            result.valueNum = cell.value
            result.valueMin = cell.minValue
            result.valueMax = cell.maxValue
            result.statsModifier = measure.statsModifier
            result.resultType = measure.resultType
            result.replicateNumber = replicate
            result.substanceId = substanceId
            result.dateCreated = new Date()
            result.resultStatus = "Pending"
            return result;
        } else {
            errors.addError(0, 0, parsed)
            return null;
        }
    }

    private ExperimentContextItem createExperimentContextItem(ItemService.Item assayItem, String stringValue, ImportSummary errors) {
        def parsed = parseContextItem(stringValue, assayItem)

        if (parsed instanceof Cell) {
            Cell cell = parsed
            ExperimentContextItem item = new ExperimentContextItem(attributeElement: assayItem.attributeElement,
                    valueElement: cell.element,
                    valueNum: cell.value,
                    valueMin: cell.minValue,
                    valueMax: cell.maxValue,
                    qualifier: cell.qualifier)
        } else {
            errors.addError(0, 0, parsed)
            return null;
        }
    }

    private void linkResults(String relationship, ImportSummary errors, int lineNumber, Result childResult, Result parentResult) {
        HierarchyType hierarchyType = HierarchyType.getByValue(relationship);
        if (hierarchyType == null) {
            // hack until values are consistent in database
            if (relationship == null) {
                hierarchyType = HierarchyType.Child;
            } else if (relationship == "has Child") {
                hierarchyType = HierarchyType.Child;
            } else if (relationship == "Derived from") {
                hierarchyType = HierarchyType.Derives;
            } else {
                errors.addError(lineNumber, 0, "Experiment measures had the relationship ${relationship} which was unrecognized");
                return;
            }
        }

        ResultHierarchy resultHierarchy = new ResultHierarchy()
        resultHierarchy.hierarchyType = hierarchyType
        resultHierarchy.result = childResult
        resultHierarchy.parentResult = parentResult
        resultHierarchy.dateCreated = new Date()
        childResult.resultHierarchiesForResult.add(resultHierarchy)
        parentResult.resultHierarchiesForParentResult.add(resultHierarchy)
    }

    InitialParse initialParse(Reader input, ImportSummary errors, Template template) {
        LineReader reader = new LineReader(new BufferedReader(input))

        // first section
        List potentialExperimentColumns = itemService.getLogicalItems(template.experiment.assay.assayContexts.collectMany {AssayContext context ->
            context.assayContextItems.findAll {it.attributeType != AttributeType.Fixed}
        })

        InitialParse result = parseConstantRegion(reader, errors, potentialExperimentColumns)
        if (errors.hasErrors())
            return

        // main header
        List<String> columns = parseTableHeader(reader, template, errors)
        if (errors.hasErrors())
            return

        def parseInt = {x->Integer.parseInt(x)}
        def parseOptInt = {x-> if(x.trim().length() > 0) { return Integer.parseInt(x) } }
        def parseLong = {x->Long.parseLong(x)}

        // all data rows
        List rows = []
        Set usedRowNumbers = [] as Set
        forEachDataRow(reader, columns, errors) { int lineNumber, List<String> values ->
            def parsed = safeParse(errors, values, lineNumber, [ parseInt, parseLong, parseOptInt, parseOptInt ])

            if (parsed == null) {
                // if we got errors parsing the fixed columns, don't proceed to the rest of the columns
                return
            }

            Integer rowNumber = parsed[0]
            Long sid = parsed[1]
            Integer replicate = parsed[2]
            Integer parentRowNumber = parsed[3]

            if (sid <= 0) {
                errors.addError(lineNumber, 0, "Invalid substance id ${sid}")
                return
            }

            if (usedRowNumbers.contains(rowNumber)) {
                errors.addError(lineNumber, 0, "Row number ${rowNumber} was duplicated")
                return
            }
            usedRowNumbers.add(rowNumber)

            Row row = new Row (lineNumber: lineNumber, rowNumber: rowNumber, replicate: replicate, parentRowNumber: parentRowNumber, sid: sid)

            // parse the dynamic columns
            for(int i=0;i<columns.size();i++) {
                String cellString = values[i+FIXED_COLUMNS.size()];
                if (cellString.isEmpty())
                    continue

                String column = columns.get(i);
                row.cells.add(new RawCell(columnName: column, value: cellString));
            }

            rows.add(row)
        }

        result.rows = rows
        result.linesParsed = reader.lineNumber
        result.topLines = reader.topLines

        return result
    }

    ImportSummary importResults(Experiment experiment, InputStream input) {
        String originalFilename = archivePathService.constructUploadResultPath(experiment)
        String exportFilename = archivePathService.constructExportResultPath(experiment)
        File archivedFile = archivePathService.prepareForWriting(originalFilename)

        OutputStream output = new GZIPOutputStream(new FileOutputStream(archivedFile));
        IOUtils.copy(input, output);
        input.close()
        output.close()

        ImportSummary summary = importResultsWithoutSavingOriginal(experiment, new GZIPInputStream(new FileInputStream(archivedFile)), originalFilename, exportFilename);
        if (summary.hasErrors()) {
            archivedFile.delete()
        }

        return summary;
    }

    Map<Measure, Collection<ItemService.Item>> constructItemsByMeasure(Experiment experiment) {
        Map<Measure, Collection<ItemService.Item>> itemsByMeasure = experiment.experimentMeasures.collectEntries { ExperimentMeasure em ->
            [em.measure,
                em.measure.assayContextMeasures.collectMany { AssayContextMeasure acm ->
                    itemService.getLogicalItems(acm.assayContext.contextItems)
                } ]
        }

        return itemsByMeasure
    }

    ImportSummary importResultsWithoutSavingOriginal(Experiment experiment, InputStream input, String originalFilename, String exportFilename) {
        ImportSummary errors = new ImportSummary()

        Template template = generateMaxSchema(experiment)
        Map<Measure, Collection<ItemService.Item>> itemsByMeasure = constructItemsByMeasure(experiment)

        def parsed = initialParse(new InputStreamReader(input), errors, template)
        if (parsed != null && !errors.hasErrors()) {
            errors.linesParsed = parsed.linesParsed

            // populate the top few lines in the summary.
            errors.topLines = parsed.topLines

            def missingSids = pugService.validateSubstanceIds( parsed.rows.collect {it.sid} )

            missingSids.each {
                errors.addError(0, 0, "Could not find substance with id ${it}")
            }

            if (!errors.hasErrors())
            {
                def results = createResults(parsed.rows, experiment.experimentMeasures, errors, itemsByMeasure)

                if (!errors.hasErrors()) {
                    // and persist these results to the DB
                    Collection<ExperimentContext>contexts = parsed.contexts;

                    persist(experiment, results, errors, contexts, originalFilename, exportFilename)
                }
            }
        }

        return errors
    }

    private void persist(Experiment experiment, Collection<Result> results, ImportSummary errors, List<ExperimentContext> contexts, String originalFilename, String exportFilename) {
        deleteExperimentResults(experiment)

        results.each {
            String label = it.displayLabel
            Integer count = errors.resultsPerLabel.get(label)
            if (count == null) {
                count = 0
            }
            errors.resultsPerLabel.put(label, count + 1)

            errors.substanceIds.add(it.substanceId)

            if (it.resultHierarchiesForParentResult.size() > 0 || it.resultHierarchiesForResult.size() > 0)
                errors.resultsWithRelationships ++;

            errors.resultAnnotations += it.resultContextItems.size()
        }

        contexts.each {
            it.experiment = experiment
            experiment.addToExperimentContexts(it)

            errors.experimentAnnotationsCreated += it.contextItems.size()
        }

        errors.resultsCreated = results.size()

        resultsExportService.dumpFromList(exportFilename, results)

        addExperimentFileToDb(experiment, originalFilename, exportFilename)
    }

    private addExperimentFileToDb(Experiment experiment, String originalFilename, String exportFilename) {
        ExperimentFile file = new ExperimentFile(experiment: experiment, originalFile: originalFilename, exportFile: exportFilename, dateCreated: new Date(), submissionVersion: experiment.experimentFiles.size())
        file.save(failOnError:true)
        experiment.experimentFiles.add(file)
    }

    /* removes all data that gets populated via upload of results.  (That is, bard.db.experiment.ExperimentContextItem, bard.db.experiment.ExperimentContext, Result and bard.db.experiment.ResultContextItem */
    public void deleteExperimentResults(Experiment experiment) {
        // this is probably ridiculously slow, but my preference would be allow DB constraints to cascade the deletes, but that isn't in place.  So
        // walk the tree and delete all the objects.

        new ArrayList(experiment.experimentContexts).each { context ->
            new ArrayList(context.experimentContextItems).each { item ->
                context.removeFromExperimentContextItems(item)
                item.delete()
            }
            experiment.removeFromExperimentContexts(context)
            context.delete()
        }
    }
}
