CREATE TABLE EXPERIMENT_FILE (
    EXPERIMENT_FILE_ID  NUMBER(19,0) NOT NULL,
    EXPERIMENT_ID       NUMBER(19,0) NOT NULL,
    SUBMISSION_VERSION  NUMBER(19,0) NOT NULL,
    ORIGINAL_FILE       VARCHAR2(1000 CHAR) NOT NULL,
    EXPORT_FILE         VARCHAR2(1000 CHAR),
    VERSION                 NUMBER(38, 0)     DEFAULT 0 NOT NULL,
    DATE_CREATED            TIMESTAMP(6)      DEFAULT sysdate NOT NULL,
    Last_Updated            TIMESTAMP(6),
    MODIFIED_BY             VARCHAR2(40)
)
;

CREATE INDEX FK_EXPERIMENT_FILE ON EXPERIMENT_FILE(EXPERIMENT_ID)
;

ALTER TABLE EXPERIMENT_FILE ADD CONSTRAINT FK_EXPERIMENT_FILE
    FOREIGN KEY (EXPERIMENT_ID)
    REFERENCES EXPERIMENT(EXPERIMENT_ID)
;

CREATE SEQUENCE EXPERIMENT_FILE_ID_SEQ
START WITH 1
INCREMENT BY 1
NOMINVALUE
MAXVALUE 2147483648
NOCYCLE
CACHE 2
NOORDER
;
