package format.text;

import format.text.CommonFormats;
import format.text.LargeFormat;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public class LargeFastaTest {
    static int count = 0;

    @Test
    public void test() {

        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");
        try (InputStream inputStream = new FileInputStream(pathToFile.toFile())) {

            LargeFormat largeFasta = CommonFormats.LARGE_FASTA;

            largeFasta.iterateRecords(inputStream, pathToFile.resolveSibling(".test.tmp")).forEach(is -> readOut(is));
            System.out.println(count * 2);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void readOut(InputStream inputStream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            System.out.println(bufferedReader.lines().collect(Collectors.joining("\n")));
            count++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
