package db.derby;

import alphabet.translate.GStreamRibosome;
import alphabet.translate.GeneticCode;
import blast.blast.BlastHelper;
import blast.ncbi.output.BlastOutput;
import blast.ncbi.output.Iteration;
import format.text.CommonFormats;
import format.text.LargeFormat;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import sequence.nucleotide.genome.LargeGenome;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

/**
 * Created by alext on 4/1/15.
 */
public class GDerbyEmbeddedConnectorTest5 {

    protected static int chromosomeID;
    protected static LargeFormat largeFormat;
    protected static GDerbyEmbeddedConnector connector;
    protected static Integer genomeID;
    protected static Path testGenomePath;
    protected static Path testBlastOutput;
    protected static Path tmpBlastOutput;
    protected static LargeGenome lg;
    protected static String genomeName;
    protected static Path toTmp;
    protected static int batchSize;
    protected static int balancer;
    protected static int minLen;
    protected static int maxLen;
    protected static BlastOutput blastOutput;
    protected static int[]orf_ids_arr;

    @BeforeClass
    public static void setUp() throws Exception {
        System.out.println("setUp()");
        largeFormat= CommonFormats.LARGE_FASTA;
        connector = GDerbyEmbeddedConnector.get("jdbc:derby:testres/db/derby/testdb;", "gblaster", "gblaster");
        connector.connectToDatabase();
        connector.getConnection().setAutoCommit(false);
        testGenomePath = Paths.get("testres/db/genome/toxoplasma_gondii_m49.fasta");
        genomeName = "Toxoplasma gondii M49";
        testBlastOutput=Paths.get("testres/db/blast/test.blast.xml");
        tmpBlastOutput=testBlastOutput.resolveSibling("test.blast.xml.tmp");
        toTmp = Paths.get("/tmp");
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(testGenomePath.toFile()))) {
            lg = LargeGenome.grasp(genomeName, inputStream, largeFormat, toTmp);
            genomeID = connector.saveLargeGenome(lg);
            System.out.println("Genome id: "+genomeID);
            chromosomeID=connector.saveLargeChromosomesForGenomeId(genomeID,lg.stream(),batchSize).limit(1).sum();
            System.out.println("Chromosome id: "+chromosomeID);
        }
        chromosomeID=connector.loadChromosomeIdsForGenomeId(genomeID).limit(1).sum();
        batchSize=100;
        balancer=100;
        minLen=20;
        maxLen=10000;

        InputStream inputStream=connector.loadLargeCrhomosomeForID(chromosomeID,largeFormat).get().getSequenceInputstream();
        GStreamRibosome gStreamRibosome = alphabet.translate.GStreamRibosome.newInstance(inputStream, GeneticCode.STANDARD);
        IntStream orf_ids=connector.saveOrfsForChromosomeId(chromosomeID, gStreamRibosome.translate()
                .filter(orf -> orf.getSequence().length() >= minLen)
                .filter(orf -> orf.getSequence().length() <= maxLen), batchSize);
        orf_ids_arr=orf_ids.toArray();
        System.out.println("ORFS parsed: "+orf_ids_arr.length);
        System.out.println("setUp() done.");
    }

    @Test
    public void TestSaveBLASTResultBatchF() throws Exception{
        try(BufferedReader bufferedReader=new BufferedReader(new FileReader(testBlastOutput.toFile()));
        BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(tmpBlastOutput.toFile()))){
            String line;
            while((line=bufferedReader.readLine())!=null){
                bufferedWriter.write(
                        line
                                .replaceAll("gi\\|",String.valueOf(orf_ids_arr[0]).concat("|"))
                                .replaceAll("sp\\|", String.valueOf(orf_ids_arr[0]).concat("|"))
                                .replaceAll("<Hit_def>", "<Hit_def>"+String.valueOf(orf_ids_arr[0]).concat("|"))
                );
                bufferedWriter.newLine();
            }
        }
       try(BufferedInputStream bufferedInputStream=new BufferedInputStream(new FileInputStream(tmpBlastOutput.toFile()))){
           blastOutput= BlastHelper.catchBLASTOutput(bufferedInputStream);
           Assert.assertTrue(connector.saveBlastResultBatch(blastOutput.getBlastOutputIterations().getIteration().stream(),genomeID,genomeID));
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
