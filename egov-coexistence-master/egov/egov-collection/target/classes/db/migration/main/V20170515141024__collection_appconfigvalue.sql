DELETE FROM eg_appconfig_values WHERE  KEY_ID=(select id from eg_appconfig where KEY_NAME ='COLLECTIONROLEFORNONEMPLOYEE') AND VALUE='Bank Collection Operator';
INSERT into eg_appconfig_values (ID,KEY_ID,EFFECTIVE_FROM,VALUE,CREATEDDATE,LASTMODIFIEDDATE,CREATEDBY,LASTMODIFIEDBY,VERSION) values (nextval('seq_eg_appconfig_values'),(select id from eg_appconfig where KEY_NAME ='COLLECTIONROLEFORNONEMPLOYEE'),now(),'Bank Collection Operator',now(),now(),(select id from eg_user where username='egovernments'),(select id from eg_user where username='egovernments'),0);