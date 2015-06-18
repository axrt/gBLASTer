package fix;

import db.BlastDAO;
import db.GenomeDAO;
import db.connect.Connector;
import db.derby.GDerbyEmbeddedConnector;
import gblaster.main;
import org.junit.Test;
import org.xml.sax.SAXException;
import properties.PropertiesLoader;
import properties.jaxb.GBlasterProperties;
import properties.jaxb.Genome;
import static java.lang.System.out;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.sql.SQLException;

/**
 * Created by alext on 4/20/15.
 */
public class TestFailingBHPair {
    @Test
    public void test(){
        try(InputStream inputStream=new BufferedInputStream(new FileInputStream(main.propertiesFile))){


            final GBlasterProperties gBlasterProperties = PropertiesLoader.load(inputStream);
            final Connector connector = GDerbyEmbeddedConnector.get("jdbc:derby:/home/alext/Documents/Research/gBLASTer/db/gblasterdb;create=true", "gblaster", "gblaster");
            connector.connectToDatabase();
            connector.getConnection().setAutoCommit(false);
            final BlastDAO blastDAO = (BlastDAO)connector;
            final Genome genomeOne=gBlasterProperties.getGenome().get(25);
            final Genome genomeTwo=gBlasterProperties.getGenome().get(11);

            out.println(genomeOne.getName().getName());
            out.println(genomeTwo.getName().getName());
            main.unloadBHForGenomePair(
                    genomeOne,
                    genomeTwo,
                    main.orfUnloadBalancer, (GenomeDAO)connector, blastDAO,
                    main.bhFolder, main.bitscoreCutoff);

        }catch (IOException e) {
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
}
