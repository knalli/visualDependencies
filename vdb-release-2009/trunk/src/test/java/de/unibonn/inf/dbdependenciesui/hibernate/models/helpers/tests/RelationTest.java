package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.tests;

import junit.framework.JUnit4TestAdapter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;

public class RelationTest {

	@Before
	public void setUpBefore() throws Exception {
		// Need no hibernate setup.
		// HibernateTestingUtil.overrideConfiguration(true);
	}

	/**
	 * Return the suite adapter.
	 * 
	 * This is used for test suites - a workaround for jUnit 3.x Test Suites.
	 * 
	 * @return
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(RelationTest.class);
	}

	/**
	 * Test case for constructor with nulls
	 * 
	 * expected NullPointerException
	 */
	@Test(expected = NullPointerException.class)
	public final void testRelationConstructor1() {
		new Relation(new DatabaseTable(), null, true);
	}

	/**
	 * Test case for constructor
	 * 
	 * source: table1 target: table2 isFromSource: true/false
	 * 
	 */
	@Test
	public final void testRelationConstructor2() {
		Relation relation = null;
		final DatabaseTable table1 = new DatabaseTable();
		final DatabaseTable table2 = new DatabaseTable();
		table1.setTitle("TABLE1");
		table2.setTitle("TABLE2");

		// Relation #1
		relation = new Relation(table1, table2, true);

		Assert.assertNotNull(relation);

		// Check source
		Assert.assertEquals("Check source entity.", "TABLE1", relation
				.getSourceName());
		// Check target
		Assert.assertEquals("Check target entity.", "TABLE2", relation
				.getTargetName());
		// Check isFromSource
		Assert
				.assertTrue("Check is source attribute.", relation
						.isFromSource());

		// Relation #2
		relation = new Relation(table1, table2, false);

		Assert.assertNotNull(relation);

		// Check source
		Assert.assertEquals("Check source entity.", "TABLE1", relation
				.getSourceName());
		// Check target
		Assert.assertEquals("Check target entity.", "TABLE2", relation
				.getTargetName());
		// Check isFromSource
		Assert.assertFalse("Check is source attribute.", relation
				.isFromSource());
	}

	/**
	 * Test case for getName (is always the source)
	 * 
	 * source: table1, target: table2, isFromSource: true/false
	 */
	@Test
	public final void testGetName() {
		Relation relation = null;

		// The source is table1.
		relation = new Relation("TABLE1", "TABLE2", true);

		Assert.assertEquals("Check the relation's name", "TABLE1", relation
				.getName());

		// The soure is table2.
		relation = new Relation("TABLE1", "TABLE2", false);

		Assert.assertEquals("Check the relation's name", "TABLE2", relation
				.getName());
	}

	/**
	 * Test case for setName (overriding constructor's setting)
	 * 
	 * source: table1, target: table2, isFromSource: true, newName: XYZ
	 */
	@Test
	public final void testSetName() {
		Relation relation = null;
		relation = new Relation("TABLE1", "TABLE2", true);
		relation.setName("XYZ");

		Assert.assertEquals("Check the relation's name", "XYZ", relation
				.getName());
	}

	/**
	 * Tests by {@link #testRelationConstructor2()}
	 */
	public final void testGetTargetName() {
	}

	/**
	 * Interaction with database connection.
	 */
	public final void testGetSourceTable() {
	}

	/**
	 * Interaction with database connection.
	 */
	public final void testGetTargetTable() {
	}

	/**
	 * Tests by {@link #testRelationConstructor2()}
	 */
	public final void testGetSourceName() {
	}

	/**
	 * Test case for getColumn
	 * 
	 * source: table1, target: table2, isFromSource: true, column: ABC
	 */
	@Test
	public final void testGetColumn() {
		Relation relation = null;
		relation = new Relation("TABLE1", "TABLE2", true);
		relation.setColumn("ABC");

		Assert.assertEquals("Check column.", "ABC", relation.getColumn());
	}

	/**
	 * Test case for getCondition
	 * 
	 * source. table1, target: table2, isFromSource: true, condition: ABC
	 */
	@Test
	public final void testGetCondition() {
		Relation relation = null;
		relation = new Relation("TABLE1", "TABLE2", true);
		relation.setCondition("ABC");

		Assert.assertEquals("Check column.", "ABC", relation.getCondition());
	}

	/**
	 * Test case for isView
	 * 
	 * source: table1, target: table2, isFromSource: true, isView: true/false
	 * 
	 * The default value of isView should be false. Test both true/false
	 * possibilities.
	 */
	@Test
	public final void testIsView() {
		Relation relation = null;
		relation = new Relation("TABLE1", "TABLE2", true);

		Assert.assertFalse("Check is view: standard is false", relation
				.isView());

		relation.setView(true);
		Assert.assertTrue("Check is view: is true", relation.isView());

		relation.setView(false);
		Assert.assertFalse("Check is view: is false", relation.isView());
	}

	/**
	 * Test case for isPositive
	 * 
	 * source: table1, target: table2, isFromSource: true, isPositive:
	 * true/false
	 * 
	 * The default value of isPositive should be false. Test both true/false
	 * possibilities.
	 */
	@Test
	public final void testIsPositive() {
		Relation relation = null;
		relation = new Relation("TABLE1", "TABLE2", true);

		Assert.assertFalse("Check is positive: standard is false", relation
				.isPositive());

		relation.setPositive(true);
		Assert.assertTrue("Check is positive: is true", relation.isPositive());

		relation.setPositive(false);
		Assert
				.assertFalse("Check is positive: is false", relation
						.isPositive());
	}

	/**
	 * Test case for isAndCondition
	 * 
	 * source: table1, target: table2, isFromSource: true, isView: true/false
	 * 
	 * The default value of isAndCondition should be false. Test both true/false
	 * possibilities.
	 */
	@Test
	public final void testIsAndCondition() {
		Relation relation = null;
		relation = new Relation("TABLE1", "TABLE2", true);

		Assert.assertFalse("Check is and: standard is false", relation
				.isAndCondition());

		relation.setAndCondition(true);
		Assert.assertTrue("Check is and: is true", relation.isAndCondition());

		relation.setAndCondition(false);
		Assert.assertFalse("Check is and: is false", relation.isAndCondition());
	}

	/**
	 * Tested {@link #testRelationConstructor2()}
	 */
	public final void testIsFromSource() {

	}

}
