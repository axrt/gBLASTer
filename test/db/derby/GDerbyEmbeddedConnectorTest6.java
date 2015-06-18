package db.derby;


import db.BlastDAO;
import db.GenomeDAO;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import properties.jaxb.Genome;
import properties.jaxb.Name;


/**
 * Created by alext on 6/18/15.
 */
public class GDerbyEmbeddedConnectorTest6 {

    protected static GDerbyEmbeddedConnector connector;
    protected static int queryGenomeID;
    protected static int targetGenomeID;
    protected static String queryGenomeName;
    protected static String targetGenomeName;

    @BeforeClass
    public static void setUp() throws Exception {

        System.out.println("setUp()");
        connector = GDerbyEmbeddedConnector.get("jdbc:derby:testres/db/derby/testdb;", "gblaster", "gblaster");
        connector.connectToDatabase();
        queryGenomeName="Escherichia_coli_str_K_12_substr_MG1655_complete_genome";
        targetGenomeName="Bacillus_subtilis_subsp_subtilis_str_168_chromosome_complete_genome";
    }

    @Test
    public void testProgress(){

        final GenomeDAO genomeDAO = (GenomeDAO) connector;
        final BlastDAO blastDAO = (BlastDAO) connector;
        try {

            queryGenomeID=genomeDAO.saveGenomeForName(queryGenomeName);
            targetGenomeID=genomeDAO.saveGenomeForName(targetGenomeName);

            final Genome mockQueryGenome=new Genome();
            final Name mockQueryGenomeName=new Name();
            mockQueryGenomeName.setName(queryGenomeName);
            mockQueryGenome.setName(mockQueryGenomeName);

            final Genome mockTargetGenome=new Genome();
            final Name mockTargetGenomeName=new Name();
            mockTargetGenomeName.setName(targetGenomeName);
            mockTargetGenome.setName(mockTargetGenomeName);

            final int idProgress=blastDAO.setBlastedPair(mockQueryGenome,mockTargetGenome);

            Assert.assertTrue(blastDAO.genomeHasBeenBlastedOver(mockQueryGenome,mockTargetGenome));

            //Assert.assertEquals();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {


    }
}
