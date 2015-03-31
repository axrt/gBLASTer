package db.derby;

import junit.framework.TestCase;

public class DerbyEmbeddedConnectorTest extends TestCase {



    public void testLoadDriver() throws Exception {
        final DerbyEmbeddedConnector connector=DerbyEmbeddedConnector.newDefaultInstance("jdbc:derby:db/testdb;create=true", "gblaster", "gblaser");
        TestCase.assertEquals(true,connector.loadDriver());
    }

    public void testNewDefaultInstance() throws Exception {
        TestCase.assertNotNull(DerbyEmbeddedConnector.newDefaultInstance("jdbc:derby:db/testdb;create=true", "gblaster", "gblaser"));
    }

    public void testConnectToDatabase() throws Exception {
        final DerbyEmbeddedConnector connector=DerbyEmbeddedConnector.newDefaultInstance("jdbc:derby:testres/db/derby/testdb;create=true", "gblaster", "gblaser");
        TestCase.assertEquals(true, connector.connectToDatabase());
    }
}