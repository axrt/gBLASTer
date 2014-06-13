package db.mysql;

import alphabet.translate.GStreamRibosome;
import alphabet.translate.GeneticCode;
import db.GenomeDAO;
import db.OrfDAO;
import format.text.CommonFormats;
import junit.extensions.TestSetup;
import org.junit.Test;
import sequence.nucleotide.genome.LargeGenome;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
            System.out.println(gd.saveLargeGenomeForName(chromosomes));

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
            gd.saveLargeChromosomes(2, chromosomes.stream()).forEach(System.out::println);
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
            gd.loadLargeChromosomesForGemomeID(2, CommonFormats.LARGE_FASTA).forEach(ch -> System.out.println(ch.getSequence()));
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
                                                            .translate()).forEach(System.out::println);
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

    @Test
    public void loadAllOrfsForGenomeIdTest(){
        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");
        try {

            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;
            final OrfDAO od = (OrfDAO) mySQLConnector;
            od.loadAllOrfsForGenomeId(2).forEach(orf->System.out.println(orf.getAc()));

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}