package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllHelpersTests {

	public static Test suite() {
		final TestSuite suite = new TestSuite(
				"Test for de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.test");
		suite.setName("Test suite of model helpers.");
		// $JUnit-BEGIN$
		suite.addTest(ColumnTest.suite());
		suite.addTest(DdlSchemaTest.suite());
		suite.addTest(GraphPropertiesTest.suite());
		suite.addTest(RelationTest.suite());
		// $JUnit-END$
		return suite;
	}

}
