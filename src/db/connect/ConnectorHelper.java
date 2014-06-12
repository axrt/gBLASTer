package db.connect;
//TODO document
/**
 * A noninstantiable class that stores static variables for the Connection class
 */
public class ConnectorHelper {

    private ConnectorHelper() {
        throw new AssertionError();
    }

    /**
     * "user" workd for database login
     */
    public static final String user = "user";

    /**
     * "password" for database login
     */
    public static final String password = "password";
}
