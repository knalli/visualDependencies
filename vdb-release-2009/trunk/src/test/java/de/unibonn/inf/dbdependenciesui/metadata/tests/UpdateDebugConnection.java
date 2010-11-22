package de.unibonn.inf.dbdependenciesui.metadata.tests;

import javax.transaction.NotSupportedException;

import de.unibonn.inf.dbdependenciesui.TestFixtures;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;

/**
 * Stores the model of {@link TestFixtures#createOnlineDatabaseModels02()}.  
 *
 */
public class UpdateDebugConnection {

	public static void main(final String[] args) throws NotSupportedException {
		final DatabaseConnection connectionActual = TestFixtures.createOnlineDatabaseModels02();
		TestFixtures.writeDatabaseConnection(connectionActual);
	}
}
