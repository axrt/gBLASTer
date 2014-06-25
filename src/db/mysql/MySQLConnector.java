package db.mysql;

import db.connect.Connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//TODO document

/**
 * A representation of a module that connects to MySQL database
 */
public class MySQLConnector extends Connector {

    /**
     * The  driver, com.mysql.jdbc.Driver for MySQL database connection
     */
    protected static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    //protected final
    /**
     * @param URL      {@link String} of the database
     * @param user     {@link String} user name for the database
     * @param password {@link String} password for the given user
     */
    protected MySQLConnector(String URL, String user, String password) {
        super(URL, user, password);
    }

    /**
     * Loads a driver for the MySQL database
     * Not needed since jdk 1.7
     */
    @Override
    protected boolean loadDriver() throws ClassNotFoundException {
        // Dynamic class load the driver from the library
        Class.forName(MySQLConnector.MYSQL_DRIVER);
        return true;
    }

    /**
     * Connects to the remote derby database
     *
     * @return {@code true} if connected, {@code false} if smth went wrong
     * @throws java.sql.SQLException
     */
    @Override
    public boolean connectToDatabase() throws SQLException{
        this.connection = DriverManager.getConnection(this.URL, this.connectionProperties);
        if (this.connection != null) {
            return (this.connected = true);
        } else {
            return (this.connected = false);
        }
    }

    /**
     * @param URL      {@link String} of the database
     * @param user     {@link String} user name for the database
     * @param password {@link String} password for the given user
     * @return a new instance of {@link MySQLConnector} with a user name and password
     */
    public static MySQLConnector newDefaultInstance(String URL, String user, String password) {
        return new MySQLConnector(URL, user, password);
    }
}
