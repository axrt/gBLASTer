package test.blast.blast;

import blast.blast.BlastHelper;
import blast.ncbi.output.BlastOutput;
import blast.ncbi.output.Iteration;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by alext on 6/17/14.
 * TODO document class
 */
public class BlastHelperTest {

    @Test
    public void marshallTest(){

        final Path toFile= Paths.get("/home/alext/Downloads/tmp/out_13619607277eef4ff4-f7ff-4cf5-bb68-f984d5687b15");
        try(InputStream inputStream=new FileInputStream(toFile.toFile())){

            final BlastOutput blastOutput= BlastHelper.catchBLASTOutput(inputStream);
            final Iteration it=blastOutput.getBlastOutputIterations().getIteration().get(0);
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


}
