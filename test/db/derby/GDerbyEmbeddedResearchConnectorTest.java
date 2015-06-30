package db.derby;

import analisys.bbh.TripledirectionalBlastHit;
import db.ResearchDAO;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import properties.jaxb.Genome;
import properties.jaxb.Name;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by alext on 6/30/15.
 */
public class GDerbyEmbeddedResearchConnectorTest {

    protected static GDerbyEmbeddedResearchConnector connector;
    protected static String AGenomeName;
    protected static String BGenomeName;
    protected static String CGenomeName;

    @BeforeClass
    public static void setUp() throws Exception {

        System.out.println("setUp()");
        connector = GDerbyEmbeddedResearchConnector.get("jdbc:derby:/home/alext/Documents/Research/gBLASTer/db/gblasterdb;create=true;", "gblaster", "gblaster");
        connector.connectToDatabase();
        AGenomeName = "Archaeon_Loki_Lokiarch";
        BGenomeName = "Haloquadratum_walsbyi";
        CGenomeName = "Ostreococcus_tauri";

    }

    @Test
    public void testProgress(){

        final ResearchDAO researchDAO= (ResearchDAO) connector;

        try {

            final Genome mockAGenome=assembleMock(AGenomeName);
            final Genome mockBGenome=assembleMock(BGenomeName);
            final Genome mockCGenome=assembleMock(CGenomeName);

            final Stream<TripledirectionalBlastHit> hitStream=researchDAO.getTBHForGenomes(mockAGenome, mockBGenome, mockCGenome, 100);
            System.out.println(hitStream.limit(3).map(h->{
                return h.toString();
            }).collect(Collectors.joining("\n")));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Genome assembleMock(String name){
        final Genome mockQueryGenome=new Genome();
        final Name mockQueryGenomeName=new Name();
        mockQueryGenomeName.setName(name);
        mockQueryGenome.setName(mockQueryGenomeName);
        return mockQueryGenome;
    }

    @After
    public void tearDown() throws Exception {


    }
}
