CREATE TABLE OFFLINE_VALIDATION
(
   CLASS_NAME VARCHAR2(128) NOT NULL,
   DOMAIN_ID DECIMAL(19) NOT NULL,
   DATE_CREATED timestamp DEFAULT sysdate  NOT NULL,
   ERRORS VARCHAR2(4000),
   GUIDANCE VARCHAR2(4000),
   INITIATED_BY DECIMAL(19) NOT NULL
);