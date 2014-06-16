package gblaster.deploy;


import alphabet.nucleotide.NucleotideAlphabet;
import db.GenomeDAO;
import format.text.LargeFormat;
import sequence.nucleotide.genome.LargeChromosome;
import sequence.nucleotide.genome.LargeGenome;
import sequence.nucleotide.genome.LargeSequenceHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by alext on 6/16/14.
 * TODO document class
 */
public final class Deployer {
    public static final String FWD_FILE = "fwd.tmp";
    public static final String RWD_FILE = "rwd.tmp";

    private Deployer() {
        throw new AssertionError("Non-instantiable!");
    }


    public static <G extends LargeGenome> IntStream chromosomeIds(
            GenomeDAO genomeDAO, properties.jaxb.Genome genome,
            LargeFormat format, Path toTmpFolder,
            NucleotideAlphabet nucleotideAlphabet) throws Exception {

        final File genomeFile = new File(genome.getPathToFile().getPath());
        try (FileInputStream fileInputStream = new FileInputStream(genomeFile)) {
            final LargeGenome largeGenome = LargeGenome.grasp(genome.getName().getName(), fileInputStream, format, toTmpFolder);
            final int genomeId = genomeDAO.saveLargeGenome(largeGenome);
            final Stream<LargeChromosome> largeChromosomeStream = largeGenome.stream();
            final IntStream intStream = genomeDAO.saveLargeChromosomesForGenomeId(genomeId, largeChromosomeStream);
            //Calculate the RCs
            final Stream<LargeChromosome> largeChromosomes = genomeDAO.loadLargeChromosomesForGemomeID(genomeId, format);
            final Stream<LargeChromosome> largeRCChromosomes = largeChromosomes.map(lch -> {
                final File inputFile = toTmpFolder.resolve(FWD_FILE).toFile();
                if (inputFile.exists()) {
                    inputFile.delete();
                }
                final File outputFile = toTmpFolder.resolve(RWD_FILE).toFile();
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                try {
                    LargeChromosome largeChromosome = LargeChromosome.formPreprocessedComponents(
                            lch.getAc().concat("_rc"),
                            LargeSequenceHelper.revertLargeNucleotideSequence(lch.getSequenceInputstream(), nucleotideAlphabet, inputFile, outputFile),
                            format);

                    return largeChromosome;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            final IntStream rcIntStream= genomeDAO.saveLargeChromosomesForGenomeId(genomeId, largeRCChromosomes);
            return IntStream.concat(intStream,rcIntStream);
        }
    }

}
