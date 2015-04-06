package misc;

import db.derby.GDerbyEmbeddedConnector;
import format.text.CommonFormats;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by alext on 4/3/15.
 */
public class TestOsteococcus {

    protected static GDerbyEmbeddedConnector connector;

    @BeforeClass
    public static void setUp() throws Exception {
        connector = GDerbyEmbeddedConnector.get("jdbc:derby:/home/alext/Documents/Research/gBLASTer/db/gblasterdb;", "gblaster", "gblaster");
        connector.connectToDatabase();
        connector.getConnection().setAutoCommit(false);
    }

    @Test
    public void testOsteococcus() throws Exception {
        connector.loadLargeChromosomesForGenomeID(2, CommonFormats.LARGE_FASTA).forEach(lc -> {
            System.out.println(lc.getAc());
        });
    }

}
