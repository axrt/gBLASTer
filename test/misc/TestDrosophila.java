package misc;

import alphabet.nucleotide.NucleotideAlphabet;
import format.text.CommonFormats;
import gblaster.deploy.Deployer;
import org.junit.BeforeClass;
import org.junit.Test;
import sequence.nucleotide.genome.LargeChromosome;
import sequence.nucleotide.genome.LargeGenome;
import sequence.nucleotide.genome.LargeSequenceHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by alext on 4/3/15.
 */
public class TestDrosophila {

    protected static Path toFile;
    protected static LargeGenome lg;
    protected static Path toTmpFolder;
    protected static NucleotideAlphabet nucleotideAlphabet;

    @BeforeClass
    public static void setUp() throws Exception {
        toTmpFolder=Paths.get("/tmp");
        toFile= Paths.get("/home/alext/Documents/Research/gBLASTer/genomes/Drosophila_simulans.fasta");
        lg=LargeGenome.grasp("DrosophTest",new FileInputStream(toFile.toFile()), CommonFormats.LARGE_FASTA,toTmpFolder);
        nucleotideAlphabet=NucleotideAlphabet.get();
    }

    @Test
    public void testBehavour(){
        final Set<String>acs = new HashSet<>();
        lg.stream().map(lch-> {
            final File inputFile = toTmpFolder.resolve(Deployer.FWD_FILE).toFile();
            if (inputFile.exists()) {
                inputFile.delete();
            }
            final File outputFile = toTmpFolder.resolve(Deployer.RWD_FILE).toFile();
            if (outputFile.exists()) {
                outputFile.delete();
            }
            try {
                LargeChromosome largeChromosome = LargeChromosome.formPreprocessedComponents(
                        lch.getAc().concat("_rc"),
                        LargeSequenceHelper.revertLargeNucleotideSequence(lch.getSequenceInputstream(),nucleotideAlphabet, inputFile, outputFile),
                        CommonFormats.LARGE_FASTA);

                return largeChromosome;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).forEach(lch->{
            if(!acs.add(lch.getAc())){
                System.out.println("booooo");
            }else{
                System.out.println(acs.size());
            }
        });

    }

}
