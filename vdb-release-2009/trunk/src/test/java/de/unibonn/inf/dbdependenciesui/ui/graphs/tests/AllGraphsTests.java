package de.unibonn.inf.dbdependenciesui.ui.graphs.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllGraphsTests {

	public static Test suite() {
		final TestSuite suite = new TestSuite(
				"Test for de.unibonn.inf.dbdependenciesui.tests");
		suite.setName("Test suite for graph and model issues.");
		// $JUnit-BEGIN$
		suite.addTest(GraphFixturesTest.suite());
		// $JUnit-END$
		return suite;
	}

}
