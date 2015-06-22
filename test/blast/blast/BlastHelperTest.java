package blast.blast;

import blast.blast.BlastHelper;
import blast.ncbi.output.BlastOutput;
import blast.ncbi.output.HitHsps;
import blast.ncbi.output.Iteration;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/17/14.
 * TODO document class
 */
public class BlastHelperTest {

   // @Test
    public void marshallTest() {

        final Path toFile = Paths.get("/home/alext/Downloads/tmp/out_13619607277eef4ff4-f7ff-4cf5-bb68-f984d5687b15");
        try (InputStream inputStream = new FileInputStream(toFile.toFile())) {

            final BlastOutput blastOutput = BlastHelper.catchBLASTOutput(inputStream);
            final Iteration it = blastOutput.getBlastOutputIterations().getIteration().get(0);
            BlastHelper.marshallIteration(it, System.out);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testComulativeBitScore(){

        final Path abberantFile=Paths.get("testres/blast/abberant.txt");
        final Path abberantDiffFile=Paths.get("testres/blast/abberant_diff.txt");
        try {

            final String abberantContent= Files.lines(abberantFile).collect(Collectors.joining());
            final HitHsps abberantContentHsp= BlastHelper.unmarshallHsps(
                    IOUtils.toInputStream(abberantContent.substring(abberantContent.indexOf("<Hit_hsps>"),
                            abberantContent.indexOf("</Hit_hsps>") + "</Hit_hsps>".length()))).get();
            Assert.assertEquals(33.113,BlastHelper.comulativeBitScore(abberantContentHsp),0.1);

            final String abberantDiffContent= Files.lines(abberantDiffFile).collect(Collectors.joining());
            final HitHsps abberantDiffContentHsp= BlastHelper.unmarshallHsps(
                    IOUtils.toInputStream(abberantDiffContent.substring(abberantDiffContent.indexOf("<Hit_hsps>"),
                            abberantDiffContent.indexOf("</Hit_hsps>") + "</Hit_hsps>".length()))).get();
            Assert.assertEquals(33.113+28.4906,BlastHelper.comulativeBitScore(abberantDiffContentHsp),0.1);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }
}
