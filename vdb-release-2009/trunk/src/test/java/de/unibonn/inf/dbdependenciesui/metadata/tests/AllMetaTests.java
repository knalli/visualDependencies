package de.unibonn.inf.dbdependenciesui.metadata.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllMetaTests {

	public static Test suite() {
		final TestSuite suite = new TestSuite("Test cases for meta package.");
		// $JUnit-BEGIN$
		suite.addTest(OracleAnalyzerImplTest.suite());
		suite.addTest(OracleMetaDataImplTest.suite());
		suite.addTest(OracleMetaDataImpl2Test.suite());
		suite.addTest(OracleViewSqlParserImplTest.suite());
		// $JUnit-END$
		return suite;
	}

}
