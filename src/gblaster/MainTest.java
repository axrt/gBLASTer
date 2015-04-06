package gblaster;

import alphabet.character.amino.AminoAcid;
import alphabet.translate.GeneticCode;
import db.OrfDAO;
import db.mysql.GMySQLConnector;
import db.mysql.MySQLConnector;
import gblaster.deploy.Deployer;
import org.junit.Test;
import org.xml.sax.SAXException;
import properties.PropertiesLoader;
import properties.jaxb.GBlasterProperties;
import properties.jaxb.Genome;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by alext on 6/26/14.
 * TODO document class
 */
public class MainTest {

    @Test
    public void matchPairsTest() {

        try (InputStream inputStream = new FileInputStream(main.propertiesFile)) {
            //1.Load
            final GBlasterProperties gBlasterProperties = PropertiesLoader.load(inputStream);

            //2.Create a map of genomes and their corresponding genetic codes
            final Map<Genome, GeneticCode<AminoAcid>> genomeGeneticCodeMap = Deployer.mapGenomesToGeneticCode(gBlasterProperties.getGenome().stream());

            //3.Connect to database
            final MySQLConnector mySQLConnector = GMySQLConnector.get("jdbc:mysql://localhost:3306?netTimeoutForStreamingResults=60&useCursorFetch=true", "gblaster", "gblaster");

            mySQLConnector.connectToDatabase();

            final Genome[][] sorted = main.matchPairs(gBlasterProperties.getGenome(), (OrfDAO) mySQLConnector);

            System.out.println();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
