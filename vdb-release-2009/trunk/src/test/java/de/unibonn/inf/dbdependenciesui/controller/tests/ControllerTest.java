package de.unibonn.inf.dbdependenciesui.controller.tests;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import de.unibonn.inf.dbdependenciesui.TestFixtures;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.tests.HibernateTestingUtil;

public class ControllerTest {

	@Before
	public void setUpBefore() throws Exception {
		HibernateTestingUtil.overrideConfiguration(true);
	}

	/**
	 * Return the suite adapter.
	 * 
	 * This is used for test suites - a workaround for jUnit 3.x Test Suites.
	 * 
	 * @return
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ControllerTest.class);
	}

	@Test
	public void testCheckLoadingConnectionTitles() {

		Assert.assertEquals("There should be no connection", 0, Controller
				.getConnections().size());

		Controller.updateConnection(TestFixtures.createOfflineDatabaseModels01());

		Assert.assertEquals("There should be only one connection", 1,
				Controller.getConnections().size());
	}

}
