ALTER TABLE PERSON MODIFY (USERNAME NULL);

ALTER TABLE PERSON ADD NEW_OBJECT_ROLE NUMBER(19);

ALTER TABLE PERSON ADD CONSTRAINT FK_NEW_OBJECT_ROLE_CLASS FOREIGN KEY (NEW_OBJECT_ROLE) REFERENCES ROLE (ROLE_ID);