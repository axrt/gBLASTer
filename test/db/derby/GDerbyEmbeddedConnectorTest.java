package db.derby;

import format.text.CommonFormats;
import format.text.LargeFormat;
import junit.framework.TestCase;
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

    public void setUp() throws Exception {
        super.setUp();
        this.connector=GDerbyEmbeddedConnector.get("jdbc:derby:testres/db/derby/testdb;", "gblaster", "gblaster");
        this.connector.connectToDatabase();
        this.testChromosomePath= Paths.get("testres/db/genome/toxoplasma_gondii_m49.fasta");
        this.largeFormat= CommonFormats.LARGE_FASTA;
        this.genomeName="Toxoplasma gondii M49";
        this.toTmp=Paths.get("/tmp");
    }

    public void testSaveGenomeForName()throws Exception{
        try(InputStream inputStream=new BufferedInputStream(new FileInputStream(this.testChromosomePath.toFile()))){
            final LargeGenome lg=LargeGenome.grasp(this.genomeName,inputStream,largeFormat,toTmp);
            final int id_genomes=this.connector.saveLargeGenome(lg);

        }
        TestCase.assertTrue(this.connector.genomeForNameExists(genomeName));
    }

    public void testSaveLargeGenome()throws Exception{
        try(InputStream inputStream=new BufferedInputStream(new FileInputStream(this.testChromosomePath.toFile()))){
            final LargeGenome lg=LargeGenome.grasp(this.genomeName,inputStream,largeFormat,toTmp);
            this.connector.saveLargeGenome(lg);
        }
    }


    public void tearDown() throws Exception {
        this.connector.removeGenomeForName(genomeName);
        TestCase.assertFalse(this.connector.genomeForNameExists(genomeName));
        this.connector.getConnection().close();
    }
}