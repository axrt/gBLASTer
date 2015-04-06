package db.derby;

import alphabet.translate.GStreamRibosome;
import alphabet.translate.GeneticCode;
import format.text.CommonFormats;
import format.text.LargeFormat;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import properties.jaxb.Genome;
import properties.jaxb.Name;
import sequence.nucleotide.genome.LargeGenome;
import sequence.protein.ORF;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alext on 4/1/15.
 */
public class GDerbyEmbeddedConnectorTest4 {

    protected static Path toChromosomeFile;
    protected static int chromosomeID;
    protected static LargeFormat largeFormat;
    protected static GDerbyEmbeddedConnector connector;
    protected static Integer genomeID;
    protected static Path testGenomePath;
    protected static LargeGenome lg;
    protected static String genomeName;
    protected static Path toTmp;
    protected static int batchSize;
    protected static int balancer;
    protected static int minLen;
    protected static int maxLen;

    @BeforeClass
    public static void setUp() throws Exception {
        System.out.println("setUp()");
        toChromosomeFile = Paths.get("testres/db/chromosomes/ch1.fasta");
        largeFormat = CommonFormats.LARGE_FASTA;
        connector = GDerbyEmbeddedConnector.get("jdbc:derby:testres/db/derby/testdb;", "gblaster", "gblaster");
        connector.connectToDatabase();
        connector.getConnection().setAutoCommit(false);
        testGenomePath = Paths.get("testres/db/genome/toxoplasma_gondii_m49.fasta");
        genomeName = "Toxoplasma gondii M49";
        toTmp = Paths.get("/tmp");
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(testGenomePath.toFile()))) {
            lg = LargeGenome.grasp(genomeName, inputStream, largeFormat, toTmp);
            genomeID = connector.saveLargeGenome(lg);
            System.out.println("Genome id: " + genomeID);
            chromosomeID = connector.saveLargeChromosomesForGenomeId(genomeID, lg.stream(), batchSize).limit(1).sum();
            System.out.println("Chromosome id: " + chromosomeID);
        }
        chromosomeID = connector.loadChromosomeIdsForGenomeId(genomeID).limit(1).sum();
        batchSize = 100;
        balancer = 100;
        minLen = 20;
        maxLen = 10000;
        System.out.println("setUp() done.");
    }

    @Test
    public void TestSaveOrfsForChromosomeId() throws Exception {

        InputStream inputStream = connector.loadLargeCrhomosomeForID(chromosomeID, largeFormat).get().getSequenceInputstream();
        System.out.println("step 1.");
        GStreamRibosome gStreamRibosome = alphabet.translate.GStreamRibosome.newInstance(inputStream, GeneticCode.STANDARD);
        connector.saveOrfsForChromosomeId(chromosomeID, gStreamRibosome.translate()
                .filter(orf -> orf.getSequence().length() >= minLen)
                .filter(orf -> orf.getSequence().length() <= maxLen), batchSize);
        System.out.println("step 2.");

        inputStream = connector.loadLargeCrhomosomeForID(chromosomeID, largeFormat).get().getSequenceInputstream();
        gStreamRibosome = alphabet.translate.GStreamRibosome.newInstance(inputStream, GeneticCode.STANDARD);

        System.out.println("step 3.");
        final List<ORF> orfs = gStreamRibosome.translate()
                .filter(orf -> orf.getSequence().length() >= minLen)
                .filter(orf -> orf.getSequence().length() <= maxLen)
                .sorted(Comparator.comparing(ORF::getSequence)).collect(Collectors.toList());
        System.out.println("step 4.");
        final List<ORF> loadedOrfs = connector.loadAllOrfsForGenomeId(genomeID, balancer, minLen, maxLen).sorted(Comparator.comparing(ORF::getSequence))
                .collect(Collectors.toList());
        System.out.println("step 5.");

        int point = 0;
        for (ORF orf : orfs) {
            Assert.assertEquals(orf.getSequence(), loadedOrfs.get(point).getSequence());
            point++;
        }
        final Genome genome = new Genome();
        final Name name = new Name();
        name.setName(genomeName);
        genome.setName(name);
        Assert.assertEquals(loadedOrfs.size(), connector.reportORFBaseSize(genome));
        Assert.assertEquals(loadedOrfs.size(), connector.calculateOrfsForGenomeName(genomeName, minLen, maxLen));
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
