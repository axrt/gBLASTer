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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alext on 4/1/15.
 */
public class GDerbyEmbeddedConnectorTest2 {

    protected static GDerbyEmbeddedConnector connector;
    protected static Path testChromosomePath;
    protected static LargeFormat largeFormat;
    protected static String genomeName;
    protected static Path toTmp;
    protected static int genomeID;
    protected static LargeGenome lg;

    @BeforeClass
    public static void setUp() throws Exception {

        System.out.println("setUp()");
        connector = GDerbyEmbeddedConnector.get("jdbc:derby:testres/db/derby/testdb;", "gblaster", "gblaster");
        connector.connectToDatabase();
        testChromosomePath = Paths.get("testres/db/genome/toxoplasma_gondii_m49.fasta");
        largeFormat = CommonFormats.LARGE_FASTA;
        largeFormat = CommonFormats.LARGE_FASTA;
        genomeName = "Toxoplasma gondii M49";
        toTmp = Paths.get("/tmp");

    }

    @Test
    public void testSaveLoadLargeCrhomosomeForID() throws Exception {
        final int checkedID;
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(testChromosomePath.toFile()))) {
            lg = LargeGenome.grasp(genomeName, inputStream, largeFormat, toTmp);
            genomeID = connector.saveLargeGenome(lg);
            final int gid = genomeID;

            final List<Integer> generatedIDs = lg.stream().map(lc -> {
                int id = 0;
                try {
                    id = connector.saveLargeChromososmeForGenomeID(gid, lc);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return id;
            }).collect(Collectors.toList());

            checkedID = generatedIDs.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(testChromosomePath.toFile()))) {

            lg = LargeGenome.grasp(genomeName, inputStream, largeFormat, toTmp);
            final List<String> seqList = lg.stream().map(LargeChromosome::getSequence).collect(Collectors.toList());
            final String seq = connector.loadLargeCrhomosomeForID(checkedID, largeFormat).get().getSequence();

            Assert.assertEquals(seq, seqList.get(0));

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
