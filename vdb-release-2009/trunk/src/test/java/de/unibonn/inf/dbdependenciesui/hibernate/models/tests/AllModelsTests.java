package de.unibonn.inf.dbdependenciesui.hibernate.models.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllModelsTests {

	public static Test suite() {
		final TestSuite suite = new TestSuite("Test for de.unibonn.inf.dbdependenciesui.hibernate.models.test");
		suite.setName("Test suite of models.");
		// $JUnit-BEGIN$
		suite.addTest(DatabaseConnectionTest.suite());
		suite.addTest(DatabaseTableTest.suite());
		// $JUnit-END$
		return suite;
	}

}
