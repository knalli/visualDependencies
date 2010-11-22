package de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.tests;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Column;

public class ColumnTest {

	@Before
	public void setUpBefore() throws Exception {
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
		return new JUnit4TestAdapter(ColumnTest.class);
	}

	/**
	 * Test case for getName (by constructor)
	 * 
	 * name: Abcd
	 */
	@Test
	public final void testGetName() {
		final Column column = new Column("Abcd");

		Assert.assertEquals("Name has to be equal", "Abcd", column.getName());
	}

	/**
	 * Test case for getType
	 * 
	 * name: Abcd, type: VARCHAR
	 */
	@Test
	public final void testGetType() {
		final Column column = new Column("Abcd");
		column.setType("VARCHAR");

		Assert
				.assertEquals("Type has to be equal", "VARCHAR", column
						.getType());
	}

	/**
	 * Test case for getSize
	 * 
	 * name: Abcd, type: DECIMAL, size: 7
	 */
	@Test
	public final void testGetSize() {
		final Column column = new Column("Abcd");
		column.setType("DECIMAL");
		column.setSize(7);

		Assert.assertEquals("Size has to be equal", 7, column.getSize());
	}

	/**
	 * Test case for getFractionalDigits
	 * 
	 * name: Abcd, type: DCIMAL, size: 7, fractionalDigits: 5
	 */
	@Test
	public final void testGetFractionalDigits() {
		final Column column = new Column("Abcd");
		column.setType("DECIMAL");
		column.setSize(7);
		column.setFractionalDigits(5);

		Assert.assertEquals("Digits has to be equal", 5, column
				.getFractionalDigits());
	}

	/**
	 * Test case for isNullable
	 * 
	 * name: Abcd, nullable: true, default value should: false
	 */
	@Test
	public final void testIsNullable() {
		final Column column = new Column("Abcd");

		Assert.assertFalse("Nullable has to be equal", column.isNullable());

		column.setNullable(true);
		Assert.assertTrue("Nullable has to be equal", column.isNullable());
	}

}
