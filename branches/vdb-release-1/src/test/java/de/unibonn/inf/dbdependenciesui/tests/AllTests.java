package de.unibonn.inf.dbdependenciesui.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.unibonn.inf.dbdependenciesui.controller.tests.AllControllerTests;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.tests.AllHelpersTests;
import de.unibonn.inf.dbdependenciesui.hibernate.models.tests.AllModelsTests;
import de.unibonn.inf.dbdependenciesui.metadata.tests.AllMetaTests;
import de.unibonn.inf.dbdependenciesui.ui.graphs.tests.AllGraphsTests;

@RunWith(Suite.class)
@SuiteClasses( {
		AllControllerTests.class, AllModelsTests.class, AllHelpersTests.class, AllGraphsTests.class, AllMetaTests.class })
public class AllTests {}
