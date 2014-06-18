package gblaster.deploy;

import alphabet.nucleotide.NucleotideAlphabet;
import db.GenomeDAO;
import db.mysql.GMySQLConnector;
import db.mysql.MySQLConnector;
import format.text.CommonFormats;
import org.junit.Test;
import properties.jaxb.Genome;
import properties.jaxb.Name;
import properties.jaxb.PathToFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * Created by alext on 6/16/14.
 * TODO document class
 */
public class DeployerTest {

    @Test
    public void Test(){
        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");
        try {

            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost", "gblaster", "gblaster");
            mySQLConnector.connectToDatabase();
            final GenomeDAO gd = (GenomeDAO) mySQLConnector;

            final Genome genome=new Genome();
            genome.setName(new Name());
            genome.getName().setName("testname");
            genome.setPathToFile(new PathToFile());
            genome.getPathToFile().setPath(pathToFile.toFile().getPath());
            Deployer.deployAndGetchromosomeIds(gd, genome, CommonFormats.LARGE_FASTA, Paths.get("/home/alext/Downloads/tmp"), NucleotideAlphabet.get(),10)
                    .forEach(System.out::println);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
