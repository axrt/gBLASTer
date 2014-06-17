package gblaster;

import alphabet.character.amino.AminoAcid;
import alphabet.nucleotide.NucleotideAlphabet;
import alphabet.translate.GeneticCode;
import db.GenomeDAO;
import db.OrfDAO;
import db.mysql.GMySQLConnector;
import db.mysql.MySQLConnector;
import format.text.CommonFormats;
import format.text.LargeFormat;
import gblaster.deploy.Deployer;
import org.xml.sax.SAXException;
import properties.PropertiesLoader;
import properties.jaxb.GBlasterProperties;
import properties.jaxb.Genome;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class main {
    /**
     * So far this is just a runscript
     *
     * @param args
     */
    public static void main(String[] args) {

        //Load properties and map folders
        final GBlasterProperties gBlasterProperties;
        final File propertiesFile = new File("/home/alext/Developer/gBLASTer/src/properties/driver.xml");
        final Path home = Paths.get("/home/alext/Documents/gBlaster");
        final Path tmpFolder = home.resolve("tmp");
        final Path orfFolder = home.resolve("orfs");


        try (InputStream inputStream = new FileInputStream(propertiesFile)) {

            //1.Load
            gBlasterProperties = PropertiesLoader.load(inputStream);

            //2.Create a map of genomes and their corresponding genetic codes
            final Map<Genome, GeneticCode<AminoAcid>> genomeGeneticCodeMap = Deployer.mapGenomesToGeneticCode(gBlasterProperties.getGenome().stream());

            //3.Connect to database
            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");

                mySQLConnector.connectToDatabase();
                mySQLConnector.getConnection().setAutoCommit(false);


            //4.Define DAOs
            final GenomeDAO genomeDAO = (GenomeDAO) mySQLConnector;
            final OrfDAO orfDAO = (OrfDAO) mySQLConnector;

            //5.Define large format
            final LargeFormat largeFormat = CommonFormats.LARGE_FASTA;

            //6.Define nucleotide alphabet
            final NucleotideAlphabet nucleotideAlphabet = NucleotideAlphabet.get();

            //7.For each genome: deploy and translate
            try {
                gBlasterProperties.getGenome().stream().forEach(g -> {

                    try {
                        System.out.println("Deploying Genome ".concat(g.getName().getName()));
                        final IntStream chromosomeIdStream = Deployer.deployAndGetchromosomeIds(genomeDAO, g, largeFormat, tmpFolder, nucleotideAlphabet);
                        System.out.println("Translating ORFs for Genome ".concat(g.getName().getName()));
                        Deployer.translateAndGetORFStreamForGenomeId(chromosomeIdStream, genomeDAO, orfDAO, genomeGeneticCodeMap.get(g), largeFormat);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                });
                mySQLConnector.getConnection().setAutoCommit(true);

            //8.For each genome unload ORFs
                gBlasterProperties.getGenome().stream().forEach(g -> {
                    try {
                        System.out.println("Unloading ORFs for Genome ".concat(g.getName().getName()));
                        Deployer.unloadORFsForGenomeToFile(g.getName().getName(), orfDAO, genomeDAO, largeFormat, orfFolder);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });


            } catch (RuntimeException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                } else throw e;
            }


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
        }


    }
}
