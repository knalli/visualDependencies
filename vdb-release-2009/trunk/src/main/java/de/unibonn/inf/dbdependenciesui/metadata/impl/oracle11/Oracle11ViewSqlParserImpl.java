package de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11;

import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleViewSqlParserImpl;

public class Oracle11ViewSqlParserImpl extends OracleViewSqlParserImpl {
	public Oracle11ViewSqlParserImpl() {
		super(Vendor.ORACLE);
	}
}
