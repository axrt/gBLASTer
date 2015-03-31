package db.derby;

import format.text.CommonFormats;
import format.text.LargeFormat;
import junit.framework.TestCase;
import org.junit.Assert;
import sequence.nucleotide.genome.LargeGenome;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GDerbyEmbeddedConnectorTest extends TestCase {
    private GDerbyEmbeddedConnector connector;
    private Path testChromosomePath;
    private LargeFormat largeFormat;
    private String genomeName;
    private Path toTmp;
    private int genomeID;
    private LargeGenome lg;
    private int batchSize;

    public void setUp() throws Exception {
        super.setUp();
        this.connector = GDerbyEmbeddedConnector.get("jdbc:derby:testres/db/derby/testdb;", "gblaster", "gblaster");
        this.connector.connectToDatabase();
        this.testChromosomePath = Paths.get("testres/db/genome/toxoplasma_gondii_m49.fasta");
        this.largeFormat = CommonFormats.LARGE_FASTA;
        this.genomeName = "Toxoplasma gondii M49";
        this.toTmp = Paths.get("/tmp");
        this.batchSize = 100;
    }

    public void testSaveGenomeForName() throws Exception {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(this.testChromosomePath.toFile()))) {
            this.lg = LargeGenome.grasp(this.genomeName, inputStream, largeFormat, toTmp);
            this.genomeID = this.connector.saveLargeGenome(this.lg);
            TestCase.assertTrue(this.connector.genomeForNameExists(genomeName));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            this.tearDown();
        }
    }

    public void testSaveLargeChromosomesForGenomeId() throws Exception {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(this.testChromosomePath.toFile()))){
            this.lg = LargeGenome.grasp(this.genomeName, inputStream, largeFormat, toTmp);
            this.genomeID = this.connector.saveLargeGenome(this.lg);

            TestCase.assertTrue(this.connector.genomeForNameExists(genomeName));

            final int[] returnedChromosomeIDs = this.connector.saveLargeChromosomesForGenomeId(this.genomeID, this.lg.stream(), this.batchSize).toArray();
            final int[] loadedChromosomeIDs = this.connector.loadChromosomeIdsForGenomeId(this.genomeID).toArray();
            Assert.assertArrayEquals(returnedChromosomeIDs, loadedChromosomeIDs);
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        }finally {
            this.tearDown();
        }
    }

    public void tearDown() throws Exception {
        //remove genome
        this.connector.removeGenomeForName(this.genomeName);
        TestCase.assertFalse(this.connector.genomeForNameExists(this.genomeName));
        //Chromosomes will be deleted automatically
        final int[] absentChromosomes=this.connector.loadChromosomeIdsForGenomeId(this.genomeID).toArray();
        TestCase.assertEquals(absentChromosomes.length, 0);
    }
}