package common

class AssayQueries {
	final static String ASSAY_SUMMARY_BYID = "SELECT A.ASSAY_ID ADID, A.ASSAY_STATUS Status, A.ASSAY_NAME Name, A.ASSAY_SHORT_NAME ShortName, A.DESIGNED_BY DesignedBy, to_char(A.DATE_CREATED, 'MM/DD/YYYY') as DateCreated, A.ASSAY_TYPE DefinitionType, A.ASSAY_VERSION Version, to_char(A.LAST_UPDATED, 'MM/DD/YYYY') as LastUpdated, A.MODIFIED_BY ModifiedBy FROM ASSAY A WHERE A.ASSAY_ID = ?";
	static String ASSAY_SUMMARY_BYNAME = "SELECT A.ASSAY_ID adid, A.ASSAY_STATUS status, A.ASSAY_SHORT_NAME sName, A.ASSAY_NAME name, A.ASSAY_VERSION aVersion, A.ASSAY_TYPE aType, A.DESIGNED_BY designedBy FROM ASSAY A WHERE A.ASSAY_NAME=?";
	static String ASSAY_SEARCH_NAME_STR = "SELECT COUNT(A.ASSAY_NAME) Count FROM ASSAY A where A.ASSAY_NAME LIKE ?";
	String ASSAY_CONTEXT_CARDS = "SELECT AC.CONTEXT_NAME CName FROM ASSAY_CONTEXT AC WHERE AC.ASSAY_ID = ? AND AC.CONTEXT_GROUP = ?";
	final static String ASSAY_CONTEXT_ITEMS = "SELECT E.LABEL AttributeLabel, ACI.VALUE_DISPLAY ValueDisplay FROM ASSAY_CONTEXT_ITEM ACI, ELEMENT E WHERE ACI.ATTRIBUTE_ID = E.ELEMENT_ID AND ACI.ASSAY_CONTEXT_ID IN(SELECT AC.ASSAY_CONTEXT_ID ContextID FROM ASSAY_CONTEXT AC WHERE AC.ASSAY_ID = ? AND AC.CONTEXT_GROUP = ? AND AC.CONTEXT_NAME = ?)";
	String ASSAY_MEASURE = "SELECT R.RESULT_TYPE_NAME measure, S.LABEL label FROM MEASURE M INNER JOIN RESULT_TYPE_TREE R ON R.RESULT_TYPE_ID = M.RESULT_TYPE_ID INNER JOIN STATS_MODIFIER_TREE S ON S.ELEMENT_ID = M.STATS_MODIFIER_ID WHERE M.ASSAY_ID = ?";
	String ASSAY_MEASURES_LIST = "SELECT R.RESULT_TYPE_NAME measure, S.LABEL label FROM MEASURE M INNER JOIN RESULT_TYPE_TREE R ON R.RESULT_TYPE_ID = M.RESULT_TYPE_ID LEFT JOIN STATS_MODIFIER_TREE S ON S.ELEMENT_ID = M.STATS_MODIFIER_ID WHERE M.ASSAY_ID = ?";
	String ASSAY_ASSOCIATED_MEASURE_CONTEXT = "SELECT R.RESULT_TYPE_NAME measure, AC.CONTEXT_NAME context FROM ASSAY_CONTEXT_MEASURE ACM INNER JOIN MEASURE M ON M.MEASURE_ID = ACM.MEASURE_ID INNER JOIN RESULT_TYPE_TREE R ON R.RESULT_TYPE_ID = M.RESULT_TYPE_ID INNER JOIN ASSAY_CONTEXT AC ON AC.ASSAY_CONTEXT_ID = ACM.ASSAY_CONTEXT_ID WHERE AC.ASSAY_ID = ? AND R.RESULT_TYPE_NAME = ?";
	final static String ASSAY_DOCUMENT = "SELECT DOCUMENT_NAME as Name, DOCUMENT_CONTENT as Content FROM ASSAY_DOCUMENT WHERE ASSAY_ID = ? AND LOWER(DOCUMENT_TYPE) = ?";
}
