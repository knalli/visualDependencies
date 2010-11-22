package de.unibonn.inf.dbdependenciesui.metadata.impl.oracle10;

import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleTriggerSqlParserImpl;

public class Oracle10TriggerSqlParserImpl extends OracleTriggerSqlParserImpl {

	public Oracle10TriggerSqlParserImpl() {
		super(Vendor.ORACLE10);
	}

}
