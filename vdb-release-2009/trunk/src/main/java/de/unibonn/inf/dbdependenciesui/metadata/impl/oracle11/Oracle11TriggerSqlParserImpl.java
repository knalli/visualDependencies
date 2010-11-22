package de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11;

import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleTriggerSqlParserImpl;

public class Oracle11TriggerSqlParserImpl extends OracleTriggerSqlParserImpl {
	public Oracle11TriggerSqlParserImpl() {
		super(Vendor.ORACLE);
	}
}
