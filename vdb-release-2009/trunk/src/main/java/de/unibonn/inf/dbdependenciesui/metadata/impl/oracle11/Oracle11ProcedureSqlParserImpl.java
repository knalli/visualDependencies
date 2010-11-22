package de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11;

import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oraclecommons.OracleProcedureSqlParserImpl;

/**
 * @author Marc Kastleiner
 */
public class Oracle11ProcedureSqlParserImpl extends OracleProcedureSqlParserImpl {
	public Oracle11ProcedureSqlParserImpl() {
		super(Vendor.ORACLE);
	}
}
