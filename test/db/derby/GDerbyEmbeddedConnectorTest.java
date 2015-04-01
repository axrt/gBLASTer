package db.derby;

import format.text.CommonFormats;
import format.text.LargeFormat;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import sequence.nucleotide.genome.LargeGenome;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GDerbyEmbeddedConnectorTest {
    private static GDerbyEmbeddedConnector connector;
    private static Path testChromosomePath;
    private static LargeFormat largeFormat;
    private static String genomeName;
    private static Path toTmp;
    private static int genomeID;
    private static LargeGenome lg;
    private static int batchSize;

    @BeforeClass
    public static void setUp() throws Exception {

        System.out.println("setUp()");
        connector = GDerbyEmbeddedConnector.get("jdbc:derby:testres/db/derby/testdb;", "gblaster", "gblaster");
        connector.connectToDatabase();
        testChromosomePath = Paths.get("testres/db/genome/toxoplasma_gondii_m49.fasta");
        largeFormat = CommonFormats.LARGE_FASTA;
        genomeName = "Toxoplasma gondii M49";
        toTmp = Paths.get("/tmp");
        batchSize = 100;
    }
    @Test
    public void testSaveGenomeForName() throws Exception {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(testChromosomePath.toFile()))) {
            lg = LargeGenome.grasp(genomeName, inputStream, largeFormat, toTmp);
            genomeID = connector.saveLargeGenome(lg);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            Assert.assertTrue(this.connector.genomeForNameExists(genomeName));
        }
    }
    @Test
    public void testSaveLargeChromosomesForGenomeId() throws Exception {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(testChromosomePath.toFile()))){
            lg = LargeGenome.grasp(genomeName, inputStream, largeFormat, toTmp);
            genomeID = connector.saveLargeGenome(lg);

            Assert.assertTrue(connector.genomeForNameExists(genomeName));

            final int[] returnedChromosomeIDs = connector.saveLargeChromosomesForGenomeId(genomeID, lg.stream(), batchSize).toArray();
            final int[] loadedChromosomeIDs = connector.loadChromosomeIdsForGenomeId(genomeID).toArray();
            Assert.assertArrayEquals(returnedChromosomeIDs, loadedChromosomeIDs);
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("tearDown()");
        //remove genome
        connector.removeGenomeForName(genomeName);
        Assert.assertFalse(connector.genomeForNameExists(genomeName));
        //Chromosomes will be deleted automatically
        final int[] absentChromosomes=connector.loadChromosomeIdsForGenomeId(genomeID).toArray();
        Assert.assertEquals(absentChromosomes.length, 0);
    }
}