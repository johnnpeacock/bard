package bardqueryapi

/**
 * Created with IntelliJ IDEA.
 * User: balexand
 * Date: 9/14/12
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */
class MolSpreadSheetData {
    def mssData = new LinkedHashMap<String,MolSpreadSheetCell> ()
    int getRowCount(){ return 2;}
    def rowPointer = new LinkedHashMap<Long,Integer>()
    List mssHeaders = new ArrayList()
}
