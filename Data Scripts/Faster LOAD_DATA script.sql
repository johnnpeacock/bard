truncate table ASSAY_CONTEXT_MEASURE ;
truncate table PROJECT_DOCUMENT ;
truncate table EXPRMT_CONTEXT_ITEM ;
delete from EXPRMT_CONTEXT ;
truncate table PRJCT_EXPRMT_CNTXT_ITEM ;
delete from PRJCT_EXPRMT_CONTEXT ;
truncate table STEP_CONTEXT_ITEM ;
delete from STEP_CONTEXT ;
truncate table PROJECT_CONTEXT_ITEM ;
delete from PROJECT_CONTEXT ;
truncate table ASSAY_CONTEXT_ITEM ;
truncate table EXPRMT_MEASURE ;
truncate table ASSAY_CONTEXT_MEASURE ;
delete from ASSAY_CONTEXT ;
--delete from DATABASECHANGELOGLOCK ;
--delete from DATABASECHANGELOG ;
truncate table UNIT_CONVERSION ;
truncate table ELEMENT_HIERARCHY ;
truncate table TREE_ROOT ;
truncate table ONTOLOGY_ITEM ;
delete from ONTOLOGY ;
truncate table LABORATORY_TREE ;
truncate table ASSAY_DOCUMENT ;
truncate table EXTERNAL_REFERENCE ;
delete from EXTERNAL_SYSTEM ;
truncate table STAGE_TREE ;
truncate table ASSAY_DESCRIPTOR_TREE ;
truncate table UNIT_TREE ;
delete from MEASURE ;
delete from ROLE ;
delete from PROJECT_STEP ;
delete from PROJECT_EXPERIMENT ;
truncate table BIOLOGY_DESCRIPTOR_TREE ;
truncate table INSTANCE_DESCRIPTOR_TREE ;
truncate table RESULT_TYPE_TREE ;
delete from PERSON_ROLE ;
delete from PERSON ;
truncate table FAVORITE ;
delete from TEAM_MEMBER ;
delete from TEAM ;
COMMIT;
--truncate table RSLT_CONTEXT_ITEM ;
--truncate table RESULT_HIERARCHY ;
--COMMIT;
--delete from RESULT ;
COMMIT;
--DROP table RUN_CONTEX_10262012225939000 ;
truncate table SUBSTANCE ;
truncate table STATEMENT_LOG ;
--DROP table RESULT_HIE_10262012225920000 ;
--DROP table RESULT_10262012225904000 ;
truncate table IDENTIFIER_MAPPING ;
truncate table ERROR_LOG ;
delete from ELEMENT ;
delete from EXPERIMENT ;
delete from ASSAY ;
delete from PROJECT ;

COMMIT;

--SELECT 'INSERT INTO ' || table_name || ' (' || COLUMNS || ') select ' || COLUMNS || ' from data_mig.' || table_name || ';'
--FROM (SELECT cols.table_name, listagg(column_name, ', ') within GROUP (ORDER BY column_id) columns
--      FROM cols, tabs
--      WHERE tabs.table_name = cols.table_name
--      AND cols.table_name NOT LIKE '%TREE'
--      AND cols.TABLE_NAME NOT LIKE 'DATA%'
--      AND cols.TABLE_NAME NOT LIKE 'TEMP%'
--      AND cols.TABLE_NAME NOT LIKE 'MIGRATION%'

--      GROUP BY cols.table_name) C
--ORDER BY Decode (table_name, 'ASSAY_CONTEXT_MEASURE', 1, 'PROJECT_DOCUMENT', 2,'EXPRMT_CONTEXT_ITEM', 3,'EXPRMT_CONTEXT', 4,
--'PRJCT_EXPRMT_CNTXT_ITEM', 5,'PRJCT_EXPRMT_CONTEXT', 6,'STEP_CONTEXT_ITEM', 7,'STEP_CONTEXT', 8,'PROJECT_CONTEXT_ITEM', 9,'PROJECT_CONTEXT', 10,
--'ASSAY_CONTEXT_ITEM', 11, 'EXPRMT_MEASURE', 12,'ASSAY_CONTEXT_MEASURE', 13,'ASSAY_CONTEXT', 14,'UNIT_CONVERSION', 15,'ELEMENT_HIERARCHY', 16,
--'TREE_ROOT', 17,'ONTOLOGY_ITEM', 18,'ONTOLOGY', 19,'LABORATORY_TREE', 20,'ASSAY_DOCUMENT', 21,'EXTERNAL_REFERENCE', 22,'EXTERNAL_SYSTEM', 23,
--'STAGE_TREE', 24,'ASSAY_DESCRIPTOR_TREE', 25,'UNIT_TREE', 26,'MEASURE', 27,'ROLE', 28,'PROJECT_STEP', 29,'PROJECT_EXPERIMENT', 30,
--'BIOLOGY_DESCRIPTOR_TREE', 31, 'INSTANCE_DESCRIPTOR_TREE', 32,'RESULT_TYPE_TREE', 33,'PERSON_ROLE', 34,'PERSON', 35,'FAVORITE', 36,
--'TEAM_MEMBER', 37,'TEAM', 38,'RSLT_CONTEXT_ITEM', 39,'RESULT_HIERARCHY', 40,'RESULT', 41,'SUBSTANCE', 42,'ELEMENT', 43,'EXPERIMENT', 44,
--'ASSAY', 45,'PROJECT', 46, 0) DESC;





--'INSERTINTO'||TABLE_NAME||'('||COLUMNS||')SELECT'||COLUMNS||'FROMDATA_MIG.'||TABLE_NAME||';'
INSERT INTO PROJECT (PROJECT_ID, PROJECT_NAME, GROUP_TYPE, DESCRIPTION, READY_FOR_EXTRACTION, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select PROJECT_ID, PROJECT_NAME, GROUP_TYPE, DESCRIPTION, READY_FOR_EXTRACTION, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.PROJECT;
INSERT INTO ASSAY (ASSAY_ID, ASSAY_STATUS, ASSAY_SHORT_NAME, ASSAY_NAME, ASSAY_VERSION, ASSAY_TYPE, DESIGNED_BY, READY_FOR_EXTRACTION, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select ASSAY_ID, ASSAY_STATUS, ASSAY_SHORT_NAME, ASSAY_NAME, ASSAY_VERSION, ASSAY_TYPE, DESIGNED_BY, READY_FOR_EXTRACTION, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.ASSAY;
INSERT INTO EXPERIMENT (EXPERIMENT_ID, EXPERIMENT_NAME, EXPERIMENT_STATUS, READY_FOR_EXTRACTION, ASSAY_ID, CONFIDENCE_LEVEL, RUN_DATE_FROM, RUN_DATE_TO, HOLD_UNTIL_DATE, DESCRIPTION, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select EXPERIMENT_ID, EXPERIMENT_NAME, EXPERIMENT_STATUS, READY_FOR_EXTRACTION, ASSAY_ID, CONFIDENCE_LEVEL, RUN_DATE_FROM, RUN_DATE_TO, HOLD_UNTIL_DATE, DESCRIPTION, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.EXPERIMENT;
INSERT INTO ELEMENT (ELEMENT_ID, ELEMENT_STATUS, LABEL, UNIT_ID, ABBREVIATION, BARD_URI, DESCRIPTION, SYNONYMS, EXTERNAL_URL, READY_FOR_EXTRACTION, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select ELEMENT_ID, ELEMENT_STATUS, LABEL, UNIT_ID, ABBREVIATION, BARD_URI, DESCRIPTION, SYNONYMS, EXTERNAL_URL, READY_FOR_EXTRACTION, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.ELEMENT;
INSERT INTO SUBSTANCE (SUBSTANCE_ID, COMPOUND_ID, SMILES, MOLECULAR_WEIGHT, SUBSTANCE_TYPE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select SUBSTANCE_ID, COMPOUND_ID, SMILES, MOLECULAR_WEIGHT, SUBSTANCE_TYPE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.SUBSTANCE;
--INSERT INTO RESULT (RESULT_ID, RESULT_STATUS, READY_FOR_EXTRACTION, EXPERIMENT_ID, RESULT_TYPE_ID, SUBSTANCE_ID, STATS_MODIFIER_ID, REPLICATE_NO, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select RESULT_ID, RESULT_STATUS, READY_FOR_EXTRACTION, EXPERIMENT_ID, RESULT_TYPE_ID, SUBSTANCE_ID, STATS_MODIFIER_ID, REPLICATE_NO, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.RESULT;
--INSERT INTO RESULT_HIERARCHY (RESULT_HIERARCHY_ID, RESULT_ID, PARENT_RESULT_ID, HIERARCHY_TYPE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select RESULT_HIERARCHY_ID, RESULT_ID, PARENT_RESULT_ID, HIERARCHY_TYPE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.RESULT_HIERARCHY;
--INSERT INTO RSLT_CONTEXT_ITEM (RSLT_CONTEXT_ITEM_ID, RESULT_ID, ATTRIBUTE_ID, VALUE_ID, DISPLAY_ORDER, EXT_VALUE_ID, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select RSLT_CONTEXT_ITEM_ID, RESULT_ID, ATTRIBUTE_ID, VALUE_ID, DISPLAY_ORDER, EXT_VALUE_ID, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.RSLT_CONTEXT_ITEM;
INSERT INTO TEAM (TEAM_ID, TEAM_NAME, LOCATION, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select TEAM_ID, TEAM_NAME, LOCATION, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.TEAM;
INSERT INTO FAVORITE (FAVORITE_ID, PERSON_ID, FAVORITE_URL, FAVORITE_TYPE, FAVORITE_NAME, DISPLAY_ORDER, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select FAVORITE_ID, PERSON_ID, FAVORITE_URL, FAVORITE_TYPE, FAVORITE_NAME, DISPLAY_ORDER, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.FAVORITE;
INSERT INTO PERSON (PERSON_ID, USERNAME, ACCOUNT_EXPIRED, ACCOUNT_LOCKED, ACCOUNT_ENABLED, PASSWORD, PASSWORD_EXPIRED, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select PERSON_ID, USERNAME, ACCOUNT_EXPIRED, ACCOUNT_LOCKED, ACCOUNT_ENABLED, PASSWORD, PASSWORD_EXPIRED, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.PERSON;
INSERT INTO PERSON_ROLE (PERSON_ROLE_ID, ROLE_ID, PERSON_ID, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select PERSON_ROLE_ID, ROLE_ID, PERSON_ID, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.PERSON_ROLE;
INSERT INTO TEAM_MEMBER (TEAM_MEMBER_ID, TEAM_ID, PERSON_ROLE_ID, MEMBERSHIP_STATUS, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select TEAM_MEMBER_ID, TEAM_ID, PERSON_ROLE_ID, MEMBERSHIP_STATUS, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.TEAM_MEMBER;
INSERT INTO PROJECT_EXPERIMENT (PROJECT_EXPERIMENT_ID, EXPERIMENT_ID, PROJECT_ID, STAGE_ID, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select PROJECT_EXPERIMENT_ID, EXPERIMENT_ID, PROJECT_ID, STAGE_ID, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.PROJECT_EXPERIMENT;
INSERT INTO PROJECT_STEP (PROJECT_STEP_ID, VERSION, NEXT_PROJECT_EXPERIMENT_ID, PREV_PROJECT_EXPERIMENT_ID, DATE_CREATED, EDGE_NAME, LAST_UPDATED, MODIFIED_BY) select PROJECT_STEP_ID, VERSION, NEXT_PROJECT_EXPERIMENT_ID, PREV_PROJECT_EXPERIMENT_ID, DATE_CREATED, EDGE_NAME, LAST_UPDATED, MODIFIED_BY from data_mig.PROJECT_STEP;
INSERT INTO ROLE (ROLE_ID, AUTHORITY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select ROLE_ID, AUTHORITY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.ROLE;
INSERT INTO MEASURE (MEASURE_ID, ASSAY_ID, RESULT_TYPE_ID, PARENT_MEASURE_ID, ENTRY_UNIT_ID, STATS_MODIFIER_ID, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select MEASURE_ID, ASSAY_ID, RESULT_TYPE_ID, PARENT_MEASURE_ID, ENTRY_UNIT_ID, STATS_MODIFIER_ID, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.MEASURE;
INSERT INTO EXTERNAL_SYSTEM (EXTERNAL_SYSTEM_ID, SYSTEM_NAME, OWNER, SYSTEM_URL, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select EXTERNAL_SYSTEM_ID, SYSTEM_NAME, OWNER, SYSTEM_URL, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.EXTERNAL_SYSTEM;
INSERT INTO EXTERNAL_REFERENCE (EXTERNAL_REFERENCE_ID, EXTERNAL_SYSTEM_ID, EXPERIMENT_ID, PROJECT_ID, EXT_ASSAY_REF, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select EXTERNAL_REFERENCE_ID, EXTERNAL_SYSTEM_ID, EXPERIMENT_ID, PROJECT_ID, EXT_ASSAY_REF, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.EXTERNAL_REFERENCE;
INSERT INTO ASSAY_DOCUMENT (ASSAY_DOCUMENT_ID, ASSAY_ID, DOCUMENT_NAME, DOCUMENT_TYPE, DOCUMENT_CONTENT, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select ASSAY_DOCUMENT_ID, ASSAY_ID, DOCUMENT_NAME, DOCUMENT_TYPE, DOCUMENT_CONTENT, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.ASSAY_DOCUMENT;
INSERT INTO ONTOLOGY (ONTOLOGY_ID, ONTOLOGY_NAME, ABBREVIATION, SYSTEM_URL, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select ONTOLOGY_ID, ONTOLOGY_NAME, ABBREVIATION, SYSTEM_URL, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.ONTOLOGY;
INSERT INTO ONTOLOGY_ITEM (ONTOLOGY_ITEM_ID, ONTOLOGY_ID, ELEMENT_ID, ITEM_REFERENCE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select ONTOLOGY_ITEM_ID, ONTOLOGY_ID, ELEMENT_ID, ITEM_REFERENCE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.ONTOLOGY_ITEM;
INSERT INTO TREE_ROOT (TREE_ROOT_ID, TREE_NAME, ELEMENT_ID, RELATIONSHIP_TYPE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select TREE_ROOT_ID, TREE_NAME, ELEMENT_ID, RELATIONSHIP_TYPE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.TREE_ROOT;
INSERT INTO ELEMENT_HIERARCHY (ELEMENT_HIERARCHY_ID, PARENT_ELEMENT_ID, CHILD_ELEMENT_ID, RELATIONSHIP_TYPE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select ELEMENT_HIERARCHY_ID, PARENT_ELEMENT_ID, CHILD_ELEMENT_ID, RELATIONSHIP_TYPE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.ELEMENT_HIERARCHY;
INSERT INTO UNIT_CONVERSION (UNIT_CONVERSION_ID, FROM_UNIT_ID, TO_UNIT_ID, MULTIPLIER, OFFSET, FORMULA, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select UNIT_CONVERSION_ID, FROM_UNIT_ID, TO_UNIT_ID, MULTIPLIER, OFFSET, FORMULA, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.UNIT_CONVERSION;
INSERT INTO ASSAY_CONTEXT (ASSAY_CONTEXT_ID, ASSAY_ID, CONTEXT_NAME, CONTEXT_GROUP, VERSION, DISPLAY_ORDER, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select ASSAY_CONTEXT_ID, ASSAY_ID, CONTEXT_NAME, CONTEXT_GROUP, VERSION, DISPLAY_ORDER, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.ASSAY_CONTEXT;
INSERT INTO EXPRMT_MEASURE (EXPRMT_MEASURE_ID, PARENT_EXPRMT_MEASURE_ID, PARENT_CHILD_RELATIONSHIP, EXPERIMENT_ID, MEASURE_ID, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select EXPRMT_MEASURE_ID, PARENT_EXPRMT_MEASURE_ID, PARENT_CHILD_RELATIONSHIP, EXPERIMENT_ID, MEASURE_ID, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.EXPRMT_MEASURE;
INSERT INTO ASSAY_CONTEXT_ITEM (ASSAY_CONTEXT_ITEM_ID, ASSAY_CONTEXT_ID, DISPLAY_ORDER, ATTRIBUTE_TYPE, ATTRIBUTE_ID, QUALIFIER, VALUE_ID, EXT_VALUE_ID, VALUE_DISPLAY, VALUE_NUM, VALUE_MIN, VALUE_MAX, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select ASSAY_CONTEXT_ITEM_ID, ASSAY_CONTEXT_ID, DISPLAY_ORDER, ATTRIBUTE_TYPE, ATTRIBUTE_ID, QUALIFIER, VALUE_ID, EXT_VALUE_ID, VALUE_DISPLAY, VALUE_NUM, VALUE_MIN, VALUE_MAX, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.ASSAY_CONTEXT_ITEM;
INSERT INTO PROJECT_CONTEXT (PROJECT_CONTEXT_ID, PROJECT_ID, CONTEXT_NAME, CONTEXT_GROUP, DISPLAY_ORDER, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select PROJECT_CONTEXT_ID, PROJECT_ID, CONTEXT_NAME, CONTEXT_GROUP, DISPLAY_ORDER, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.PROJECT_CONTEXT;
INSERT INTO PROJECT_CONTEXT_ITEM (PROJECT_CONTEXT_ITEM_ID, PROJECT_CONTEXT_ID, ATTRIBUTE_ID, DISPLAY_ORDER, VALUE_ID, EXT_VALUE_ID, QUALIFIER, VALUE_DISPLAY, VALUE_NUM, VALUE_MIN, VALUE_MAX, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select PROJECT_CONTEXT_ITEM_ID, PROJECT_CONTEXT_ID, ATTRIBUTE_ID, DISPLAY_ORDER, VALUE_ID, EXT_VALUE_ID, QUALIFIER, VALUE_DISPLAY, VALUE_NUM, VALUE_MIN, VALUE_MAX, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.PROJECT_CONTEXT_ITEM;
INSERT INTO STEP_CONTEXT (STEP_CONTEXT_ID, PROJECT_STEP_ID, CONTEXT_NAME, CONTEXT_GROUP, DISPLAY_ORDER, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select STEP_CONTEXT_ID, PROJECT_STEP_ID, CONTEXT_NAME, CONTEXT_GROUP, DISPLAY_ORDER, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.STEP_CONTEXT;
INSERT INTO STEP_CONTEXT_ITEM (STEP_CONTEXT_ITEM_ID, STEP_CONTEXT_ID, DISPLAY_ORDER, ATTRIBUTE_ID, VALUE_ID, EXT_VALUE_ID, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select STEP_CONTEXT_ITEM_ID, STEP_CONTEXT_ID, DISPLAY_ORDER, ATTRIBUTE_ID, VALUE_ID, EXT_VALUE_ID, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.STEP_CONTEXT_ITEM;
INSERT INTO PRJCT_EXPRMT_CONTEXT (PRJCT_EXPRMT_CONTEXT_ID, PROJECT_EXPERIMENT_ID, CONTEXT_NAME, CONTEXT_GROUP, DISPLAY_ORDER, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select PRJCT_EXPRMT_CONTEXT_ID, PROJECT_EXPERIMENT_ID, CONTEXT_NAME, CONTEXT_GROUP, DISPLAY_ORDER, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.PRJCT_EXPRMT_CONTEXT;
INSERT INTO PRJCT_EXPRMT_CNTXT_ITEM (PRJCT_EXPRMT_CNTXT_ITEM_ID, PRJCT_EXPRMT_CONTEXT_ID, DISPLAY_ORDER, ATTRIBUTE_ID, VALUE_ID, EXT_VALUE_ID, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select PRJCT_EXPRMT_CNTXT_ITEM_ID, PRJCT_EXPRMT_CONTEXT_ID, DISPLAY_ORDER, ATTRIBUTE_ID, VALUE_ID, EXT_VALUE_ID, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.PRJCT_EXPRMT_CNTXT_ITEM;
INSERT INTO EXPRMT_CONTEXT (EXPRMT_CONTEXT_ID, EXPERIMENT_ID, CONTEXT_NAME, CONTEXT_GROUP, DISPLAY_ORDER, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select EXPRMT_CONTEXT_ID, EXPERIMENT_ID, CONTEXT_NAME, CONTEXT_GROUP, DISPLAY_ORDER, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.EXPRMT_CONTEXT;
INSERT INTO EXPRMT_CONTEXT_ITEM (EXPRMT_CONTEXT_ITEM_ID, EXPRMT_CONTEXT_ID, DISPLAY_ORDER, ATTRIBUTE_ID, VALUE_ID, EXT_VALUE_ID, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select EXPRMT_CONTEXT_ITEM_ID, EXPRMT_CONTEXT_ID, DISPLAY_ORDER, ATTRIBUTE_ID, VALUE_ID, EXT_VALUE_ID, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.EXPRMT_CONTEXT_ITEM;
INSERT INTO PROJECT_DOCUMENT (PROJECT_DOCUMENT_ID, PROJECT_ID, DOCUMENT_NAME, DOCUMENT_TYPE, DOCUMENT_CONTENT, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select PROJECT_DOCUMENT_ID, PROJECT_ID, DOCUMENT_NAME, DOCUMENT_TYPE, DOCUMENT_CONTENT, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.PROJECT_DOCUMENT;
INSERT INTO ASSAY_CONTEXT_MEASURE (ASSAY_CONTEXT_MEASURE_ID, ASSAY_CONTEXT_ID, MEASURE_ID, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select ASSAY_CONTEXT_MEASURE_ID, ASSAY_CONTEXT_ID, MEASURE_ID, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.ASSAY_CONTEXT_MEASURE;
INSERT INTO STATEMENT_LOG (TABLE_NAME, IDENTIFIER, ACTION_DATE, ACTION, DATA_CLAUSE) select TABLE_NAME, IDENTIFIER, ACTION_DATE, ACTION, DATA_CLAUSE from data_mig.STATEMENT_LOG;
INSERT INTO ERROR_LOG (ERROR_LOG_ID, ERROR_DATE, PROCEDURE_NAME, ERR_NUM, ERR_MSG, ERR_COMMENT) select ERROR_LOG_ID, ERROR_DATE, PROCEDURE_NAME, ERR_NUM, ERR_MSG, ERR_COMMENT from data_mig.ERROR_LOG;
--INSERT INTO MIGRATION_ACTION (ACTION_REF, COMPLETED_ACTION, COUNT_NAME, DESCRIPTION, COUNT_SQL) select ACTION_REF, COMPLETED_ACTION, COUNT_NAME, DESCRIPTION, COUNT_SQL from data_mig.MIGRATION_ACTION;
--INSERT INTO MIGRATION_PERSON (PERSON_REF, PERSON_NAME) select PERSON_REF, PERSON_NAME from data_mig.MIGRATION_PERSON;
INSERT INTO IDENTIFIER_MAPPING (TABLE_NAME, SOURCE_SCHEMA, SOURCE_ID, TARGET_ID) select TABLE_NAME, SOURCE_SCHEMA, SOURCE_ID, TARGET_ID from data_mig.IDENTIFIER_MAPPING;
--INSERT INTO MIGRATION_DAY (DAY_REF, MIGRATION_DATE, YEAR, MONTH, QUARTER, WEEK, DAY_OF_WEEK) select DAY_REF, MIGRATION_DATE, YEAR, MONTH, QUARTER, WEEK, DAY_OF_WEEK from data_mig.MIGRATION_DAY;
--INSERT INTO TEMP_CONTEXT_ITEM (DISPLAY_ORDER, EXT_ASSAY_REF, ASSAY_ID, EXPERIMENT_ID, ATTRIBUTE_ID, AID, RESULTTYPE, STATS_MODIFIER, CONTEXTITEM, CONCENTRATION, CONCENTRATIONUNIT) select DISPLAY_ORDER, EXT_ASSAY_REF, ASSAY_ID, EXPERIMENT_ID, ATTRIBUTE_ID, AID, RESULTTYPE, STATS_MODIFIER, CONTEXTITEM, CONCENTRATION, CONCENTRATIONUNIT from data_mig.TEMP_CONTEXT_ITEM;
--INSERT INTO MIGRATION_EVENT (AID, ASSAY_ID, EXPERIMENT_ID, PROJECT_ID, EVENT_COUNT, PERSON_REF, ACTION_REF, DAY_REF) select AID, ASSAY_ID, EXPERIMENT_ID, PROJECT_ID, EVENT_COUNT, PERSON_REF, ACTION_REF, DAY_REF from data_mig.MIGRATION_EVENT;
--INSERT INTO MIGRATION_AID (AID, CENTER, BAO_ANNOTATED, PROBE, DNA_REPAIR) select AID, CENTER, BAO_ANNOTATED, PROBE, DNA_REPAIR from data_mig.MIGRATION_AID;
COMMIT;

BEGIN
manage_ontology.make_trees;
END;
/


--INSERT INTO RESULT (RESULT_ID, RESULT_STATUS, READY_FOR_EXTRACTION, EXPERIMENT_ID, RESULT_TYPE_ID, SUBSTANCE_ID, STATS_MODIFIER_ID, REPLICATE_NO, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select RESULT_ID, RESULT_STATUS, READY_FOR_EXTRACTION, EXPERIMENT_ID, RESULT_TYPE_ID, SUBSTANCE_ID, STATS_MODIFIER_ID, REPLICATE_NO, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.RESULT;
--COMMIT;
--INSERT INTO RESULT_HIERARCHY (RESULT_HIERARCHY_ID, RESULT_ID, PARENT_RESULT_ID, HIERARCHY_TYPE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select RESULT_HIERARCHY_ID, RESULT_ID, PARENT_RESULT_ID, HIERARCHY_TYPE, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.RESULT_HIERARCHY;
--COMMIT;
--INSERT INTO RSLT_CONTEXT_ITEM (RSLT_CONTEXT_ITEM_ID, RESULT_ID, ATTRIBUTE_ID, VALUE_ID, DISPLAY_ORDER, EXT_VALUE_ID, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY) select RSLT_CONTEXT_ITEM_ID, RESULT_ID, ATTRIBUTE_ID, VALUE_ID, DISPLAY_ORDER, EXT_VALUE_ID, QUALIFIER, VALUE_NUM, VALUE_MIN, VALUE_MAX, VALUE_DISPLAY, VERSION, DATE_CREATED, LAST_UPDATED, MODIFIED_BY from data_mig.RSLT_CONTEXT_ITEM;
--COMMIT;

begin
RESET_SEQUENCES;
END;
/

