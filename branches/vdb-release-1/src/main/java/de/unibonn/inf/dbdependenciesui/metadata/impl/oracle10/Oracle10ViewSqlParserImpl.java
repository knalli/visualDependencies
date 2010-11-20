package de.unibonn.inf.dbdependenciesui.metadata.impl.oracle10;

import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleViewSqlParserImpl;

public class Oracle10ViewSqlParserImpl extends OracleViewSqlParserImpl {

	public Oracle10ViewSqlParserImpl() {
		super(Vendor.ORACLE10);
	}

}
