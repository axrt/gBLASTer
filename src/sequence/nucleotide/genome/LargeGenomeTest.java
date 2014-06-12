package sequence.nucleotide.genome;

import alphabet.character.amino.AminoAcid;
import alphabet.translate.GStreamRibosome;
import alphabet.translate.GeneticCode;
import format.text.CommonFormats;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by alext on 6/11/14.
 * TODO document class
 */
public class LargeGenomeTest {

    @Test
    public void test() {

        final Path pathToFile = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");
        try {

            final LargeGenome chromosomes = LargeGenome.grasp("test", new FileInputStream(pathToFile.toFile()), CommonFormats.LARGE_FASTA, pathToFile.getParent());

            final GeneticCode<AminoAcid> standard = GeneticCode.STANDARD;

            chromosomes.stream()
                    .map(ch -> GStreamRibosome.newInstance(ch.getSequenceInputstream(), standard))
                    .flatMap(gsr ->
            {
                try {
                    return gsr.translate();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            })

                    .forEach(orf -> System.out.println(orf.toString()));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
