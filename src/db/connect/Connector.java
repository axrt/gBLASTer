package db.connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
//TODO document

/**
 * An abstract class to extend for a certain type of a database used.
 */
public abstract class Connector {

    /**
     * The derby url
     */
    protected final String URL;
    /**
     * User, used as a user name when connecting the derby database
     */
    protected final String user;
    /**
     * Password, used as a password when connecting the derby database
     */
    protected final String password;
    /**
     * Database connection
     */
    protected Connection connection;
    /**
     * Properties to connect the database
     */
    protected Properties connectionProperties;
    /**
     * A flag to track whether the connection to the database has been
     * established
     */
    protected boolean connected = false;

    /**
     * @param URL      {@link String} of the database
     * @param user     {@link String} user name
     * @param password {@link String} password
     */
    protected Connector(String URL, String user,
                        String password) {
        super();
        this.URL = URL;
        this.user = user;
        this.password = password;

        // Initializing and assembling properties
        this.connectionProperties = new Properties();
        this.connectionProperties.put(ConnectorHelper.user, this.user);
        this.connectionProperties.put(ConnectorHelper.password,
                this.password);
    }

    /**
     * @return {@link java.sql.Connection} connection to the database
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Loads a driver for the database
     */
    protected abstract boolean loadDriver() throws Exception;

    /**
     * Attemts to connect to the database
     *
     * @return {@code true} in case successfully conected, {@code false} otherwise
     * @throws Exception
     */
    public boolean connectToDatabase() throws Exception {
        this.connection = DriverManager.getConnection(this.URL, this.connectionProperties);
        if (this.connection != null) {
            return (this.connected = true);
        } else {
            return (this.connected = false);
        }
    }

    /**
     * @return {@code true} in case the database connection has been
     * established, {@code false} otherwise
     */
    public boolean isConnected() {
        return this.connected;
    }

}
