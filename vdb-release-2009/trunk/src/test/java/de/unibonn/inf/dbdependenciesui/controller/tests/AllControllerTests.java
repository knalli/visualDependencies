package de.unibonn.inf.dbdependenciesui.controller.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllControllerTests {

	public static Test suite() {
		final TestSuite suite = new TestSuite(
				"Test for de.unibonn.inf.dbdependenciesui.controller.tests");
		suite.setName("Test suite for controller actions.");
		// $JUnit-BEGIN$
		suite.addTest(ControllerTest.suite());
		// $JUnit-END$
		return suite;
	}

}
