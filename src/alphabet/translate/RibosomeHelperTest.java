package alphabet.translate;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by alext on 10/6/14.
 * TODO document class
 */
public class RibosomeHelperTest {

    @Test
    public void readORFsFromFileTest(){
       final Path toFile= Paths.get("/home/alext/Documents/gBlaster/orfs/Human_mito_323_600.fasta");
        try {
            RibosomeHelper.readORFsFromFile(toFile).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
