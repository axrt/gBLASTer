package test.sequence.nucleotide.genome;

import alphabet.nucleotide.NucleotideAlphabet;
import org.junit.Test;
import sequence.nucleotide.genome.LargeSequenceHelper;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/16/14.
 * TODO document class
 */
public class LargeSequenceHelperTest {
    @Test
    public void Test() {
        final Path tmpFolder = Paths.get("/home/alext/Downloads/tmp");
        final File inpuputTmpFile=tmpFolder.resolve("fwd.tmp").toFile();
        final File outputTmpFile = tmpFolder.resolve("rwd.tmp").toFile();
        try (FileInputStream fileInputStream = new FileInputStream(new File("/home/alext/Downloads/tmp/test.nu"));
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(
                    LargeSequenceHelper.revertLargeNucleotideSequence(fileInputStream, NucleotideAlphabet.get(), inpuputTmpFile,outputTmpFile)));
            BufferedReader bufferedReader1=new BufferedReader(new FileReader(new File("/home/alext/Downloads/tmp/test.nu")));
        ){

            final StringBuilder stringBuilder = new StringBuilder(bufferedReader.lines().collect(Collectors.joining()));
            System.out.println(bufferedReader1.lines().collect(Collectors.joining()));
            System.out.println(stringBuilder.reverse().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        inpuputTmpFile.delete();
        outputTmpFile.delete();
    }


}
