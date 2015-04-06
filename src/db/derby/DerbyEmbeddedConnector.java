package db.derby;

import db.connect.Connector;

/**
 * Created by alext on 3/31/15.
 */
public class DerbyEmbeddedConnector extends Connector {

    /**
     * The  driver, org.apache.derby.jdbc.EmbeddedDriver for an embedded Derby database connection
     */
    protected static final String DERBY_EMBEDDED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    /**
     * @param URL      {@link String} of the database
     * @param user     {@link String} user name
     * @param password {@link String} password
     */
    protected DerbyEmbeddedConnector(String URL, String user, String password) {
        super(URL, user, password);
    }

    @Override
    protected boolean loadDriver() throws Exception {
        Class.forName(DERBY_EMBEDDED_DRIVER);
        return true;
    }

    public static DerbyEmbeddedConnector newDefaultInstance(String URL, String user, String password) {
        return new DerbyEmbeddedConnector(URL, user, password);
    }
}
