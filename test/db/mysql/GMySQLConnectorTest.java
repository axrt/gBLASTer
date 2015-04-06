package db.mysql;

import alphabet.translate.GStreamRibosome;
import alphabet.translate.GeneticCode;
import blast.blast.BlastHelper;
import blast.ncbi.output.BlastOutput;
import blast.ncbi.output.Iteration;
import db.BlastDAO;
import db.GenomeDAO;
import db.OrfDAO;
import format.text.CommonFormats;
import junit.extensions.TestSetup;
import org.xml.sax.SAXException;
import properties.jaxb.Genome;
import properties.jaxb.Name;
import sequence.nucleotide.genome.LargeGenome;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * Created by alext on 6/12/14.
 * TODO document class
 */
public class GMySQLConnectorTest {

    //@Test
    public void saveLargeGenomeForNameTest() {


        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");

        try {
            final LargeGenome chromosomes = LargeGenome.grasp("test", new FileInputStream(pathToFile.toFile()), CommonFormats.LARGE_FASTA, pathToFile.getParent());
            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;
            System.out.println(gd.saveLargeGenome(chromosomes));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //@Test
    public void genomeForNameExistsTest() {
        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");

        try {

            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;
            TestSetup.assertEquals(true, gd.genomeForNameExists("test"));
            TestSetup.assertEquals(false, gd.genomeForNameExists("test1"));

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void saveLargeChromososmeForGenomeIDTest() {
        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");

        try {
            final LargeGenome chromosomes = LargeGenome.grasp("test", new FileInputStream(pathToFile.toFile()), CommonFormats.LARGE_FASTA, pathToFile.getParent());
            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            mySQLConnector.getConnection().setAutoCommit(false);
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;
            chromosomes.stream().map(ch -> {
                try {
                    return gd.saveLargeChromososmeForGenomeID(2, ch);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).forEach(System.out::println);
            mySQLConnector.getConnection().setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void loadChromosomeIdsForGenomeIdTest() {
        try {

            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;
            gd.loadChromosomeIdsForGenomeId(2).forEach(System.out::println);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void saveLargeChromosomesTest() {
        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");
        try {

            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;
            final LargeGenome chromosomes = LargeGenome.grasp("test", new FileInputStream(pathToFile.toFile()), CommonFormats.LARGE_FASTA, pathToFile.getParent());
            mySQLConnector.getConnection().setAutoCommit(false);
            gd.saveLargeChromosomesForGenomeId(2, chromosomes.stream(), 10).forEach(System.out::println);
            mySQLConnector.getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //@Test
    public void loadLargeChromosomesForGemomeIDTest() {
        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");
        try {

            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;
            final LargeGenome chromosomes = LargeGenome.grasp("test", new FileInputStream(pathToFile.toFile()), CommonFormats.LARGE_FASTA, pathToFile.getParent());
            gd.loadLargeChromosomesForGenomeID(2, CommonFormats.LARGE_FASTA).forEach(ch -> System.out.println(ch.getSequence()));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void saveOrfsForChromosomeIdTest() {
        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");
        try {

            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final OrfDAO od = (OrfDAO) mySQLConnector;
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;
            final int genomeId = 2;
            mySQLConnector.getConnection().setAutoCommit(false);
            gd.loadChromosomeIdsForGenomeId(genomeId).forEach(
                    id -> {
                        try {
                            gd.loadLargeCrhomosomeForID(id, CommonFormats.LARGE_FASTA)
                                    .ifPresent(
                                            lch -> {
                                                try {
                                                    od.saveOrfsForChromosomeId(id, GStreamRibosome.newInstance(lch.getSequenceInputstream(), GeneticCode.STANDARD)
                                                            .translate(), 1000).forEach(System.out::println);
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                    );

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            mySQLConnector.getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void loadAllOrfsForGenomeIdTest() {
        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");
        try {

            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;
            final OrfDAO od = (OrfDAO) mySQLConnector;
            od.loadAllOrfsForGenomeId(2, 1000, 0, 100000).forEach(orf -> System.out.println(orf.getAc()));

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void genomeIdByNameTest() {

        try {

            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;
            TestSetup.assertEquals(gd.genomeIdByName("random"), 0);
            System.out.println(gd.genomeIdByName("testname"));

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //@Test
    public void saveBlastResultsTest() {
        final Path toFile = Paths.get("/home/alext/Downloads/tmp/out_13619607277eef4ff4-f7ff-4cf5-bb68-f984d5687b15");
        try (InputStream inputStream = new FileInputStream(toFile.toFile())) {

            final BlastOutput blastOutput = BlastHelper.catchBLASTOutput(inputStream);
            final Iteration it = blastOutput.getBlastOutputIterations().getIteration().get(0);
            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final BlastDAO blastDAO = (BlastDAO) mySQLConnector;
            blastDAO.saveBlastResult(it, 1, 2);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void genomeHasBeenBlastedOverTest() {
        final Genome query = new Genome();
        query.setName(new Name());
        query.getName().setName("ecoli");
        final Genome target = new Genome();
        target.setName(new Name());
        target.getName().setName("human_mito");
        final Genome mock = new Genome();
        mock.setName(new Name());
        mock.getName().setName("mock");
        final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
        try {
            mySQLConnector.connectToDatabase();
            final BlastDAO blastDAO = (BlastDAO) mySQLConnector;
            TestSetup.assertEquals(blastDAO.genomeHasBeenBlastedOver(query, target), true);
            TestSetup.assertEquals(blastDAO.genomeHasBeenBlastedOver(query, mock), false);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void calculateOrfsForGenomeNameTest() {
        final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
        try {
            mySQLConnector.connectToDatabase();
            final OrfDAO orfDAO = (OrfDAO) mySQLConnector;
            System.out.println(orfDAO.calculateOrfsForGenomeName("human_mito", 100, 10000));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //@Test
    public void getBBHforGenomePairTest() {
        final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
        try {
            mySQLConnector.connectToDatabase();
            final BlastDAO blastDAO = (BlastDAO) mySQLConnector;
            final Genome one = new Genome();
            one.setName(new Name());
            one.getName().setName("ecoli");
            final Genome two = new Genome();
            two.setName(new Name());
            two.getName().setName("human_mito");
            final Genome mock = new Genome();
            mock.setName(new Name());

            blastDAO.getBBHforGenomePair(one, two, Integer.MIN_VALUE).forEach(bbh -> {
                try {
                    System.out.println(bbh.getForwardHit().getIteration());
                    System.out.println(bbh.getReverseHit().getIteration());
                } catch (JAXBException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
