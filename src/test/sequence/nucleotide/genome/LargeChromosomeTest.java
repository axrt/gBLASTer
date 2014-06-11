package test.sequence.nucleotide.genome;

import format.text.CommonFormats;
import org.junit.Test;
import sequence.nucleotide.genome.LargeChromosome;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by alext on 6/11/14.
 * TODO document class
 */
public class LargeChromosomeTest {

    @Test
    public void test() {

        final Path toFile = Paths.get("/home/alext/Documents/tuit/final testing/single.fasta");
        try (PushbackInputStream inputStream = new PushbackInputStream(new FileInputStream(toFile.toFile()))) {

            final LargeChromosome lc = LargeChromosome.fromRecord(inputStream, CommonFormats.LARGE_FASTA);
            System.out.println(lc.getAc());
            System.out.println(lc.getSequence());



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
