package de.unibonn.inf.dbdependenciesui.metadata.impl.oracle10;

import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleProcedureSqlParserImpl;

/**
 * @author Marc Kastleiner
 */
public class Oracle10ProcedureSqlParserImpl extends OracleProcedureSqlParserImpl {

	public Oracle10ProcedureSqlParserImpl() {
		super(Vendor.ORACLE10);
	}

}
