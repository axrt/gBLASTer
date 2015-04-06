package db.derby;

import format.text.CommonFormats;
import format.text.LargeFormat;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import sequence.nucleotide.genome.LargeChromosome;
import sequence.nucleotide.genome.LargeGenome;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by alext on 4/1/15.
 */
public class GDerbyEmbeddedConnectorTest3 {

    protected static Path toChromosomeFile;
    protected static int chromosomeID;
    protected static LargeFormat largeFormat;
    protected static GDerbyEmbeddedConnector connector;
    protected static Integer genomeID;
    protected static Path testGenomePath;
    protected static LargeGenome lg;
    protected static String genomeName;
    protected static Path toTmp;


    @BeforeClass
    public static void setUp() throws Exception {
        System.out.println("setUp()");
        toChromosomeFile = Paths.get("testres/db/chromosomes/ch1.fasta");
        largeFormat = CommonFormats.LARGE_FASTA;
        connector = GDerbyEmbeddedConnector.get("jdbc:derby:testres/db/derby/testdb;", "gblaster", "gblaster");
        connector.connectToDatabase();
        testGenomePath = Paths.get("testres/db/genome/toxoplasma_gondii_m49.fasta");
        genomeName = "Toxoplasma gondii M49";
        toTmp = Paths.get("/tmp");
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(testGenomePath.toFile()))) {
            lg = LargeGenome.grasp(genomeName, inputStream, largeFormat, toTmp);
            genomeID = connector.saveLargeGenome(lg);

        }
    }

    @Test
    public void testGenomeIDByChromosomeID() throws Exception {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(toChromosomeFile.toFile()))) {
            final LargeChromosome lc = LargeChromosome.fromRecord(bufferedInputStream, largeFormat);
            chromosomeID = connector.saveLargeChromososmeForGenomeID(genomeID, lc);
            Assert.assertEquals(connector.genomeIDByChromosomeID(chromosomeID).get(), genomeID);
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
        final int[] absentChromosomes = connector.loadChromosomeIdsForGenomeId(genomeID).toArray();
        Assert.assertEquals(absentChromosomes.length, 0);
    }
}
