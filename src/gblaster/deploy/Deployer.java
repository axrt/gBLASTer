package gblaster.deploy;


import alphabet.character.amino.AminoAcid;
import alphabet.nucleotide.NucleotideAlphabet;
import alphabet.translate.GStreamRibosome;
import alphabet.translate.GeneticCode;
import db.ChromosomeDAO;
import db.GenomeDAO;
import db.OrfDAO;
import format.text.Format;
import format.text.LargeFormat;
import properties.jaxb.Genome;
import sequence.nucleotide.genome.LargeChromosome;
import sequence.nucleotide.genome.LargeGenome;
import sequence.nucleotide.genome.LargeSequenceHelper;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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


    public static IntStream deployAndGetchromosomeIds(
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
            final IntStream rcIntStream = genomeDAO.saveLargeChromosomesForGenomeId(genomeId, largeRCChromosomes);
            return IntStream.concat(intStream, rcIntStream);
        }
    }

    public static void translateAndGetORFStreamForGenomeId(
            IntStream chromosomeIds,
            ChromosomeDAO chromosomeDAO,
            OrfDAO orfDAO,
            GeneticCode<AminoAcid> geneticCode,
            LargeFormat chromosomeFormat
    ) throws Exception {

        chromosomeIds.forEach(chid -> {
            try {
                final Optional<LargeChromosome> largeChromosomeOptional = chromosomeDAO.loadLargeCrhomosomeForID(chid, chromosomeFormat);
                if (largeChromosomeOptional.isPresent()) {
                    final LargeChromosome largeChromosome = largeChromosomeOptional.get();
                    final GStreamRibosome gStreamRibosome = GStreamRibosome.newInstance(largeChromosome.getSequenceInputstream(), geneticCode);
                    orfDAO.saveOrfsForChromosomeId(chid, gStreamRibosome.translate());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static Map<Genome, GeneticCode<AminoAcid>> mapGenomesToGeneticCode(Stream<Genome> genomes) {
        final Map<Genome, GeneticCode<AminoAcid>> genomeGeneticCodeMap = new HashMap<>();
        genomes.forEach(g -> {
            if (g.getGeneticTable().getAlterCodon().isEmpty()) {
                genomeGeneticCodeMap.put(g, GeneticCode.STANDARD);
            } else {
                final GeneticCode<AminoAcid> geneticCode = GeneticCode.altered(g.getName().getName());
                genomeGeneticCodeMap.put(g, geneticCode);
            }
        });
        return genomeGeneticCodeMap;
    }

    public static File unloadORFsForGenomeToFile(String genomeName, OrfDAO orfDAO, GenomeDAO genomeDAO,Format format, Path dir) throws Exception {

        final File toUnload = dir.resolve(genomeName).toFile();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(toUnload))) {
            final int genomeId = genomeDAO.genomeIdByName(genomeName);
            if (genomeId == 0) {
                throw new Exception("No genome for name: ".concat(genomeName));
            }
            orfDAO.loadAllOrfsForGenomeId(genomeId).forEach(orf->{
                try {
                    bufferedWriter.write(format.formatORF(orf));
                    bufferedWriter.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (RuntimeException e){
            if(e.getCause() instanceof IOException){
                throw (IOException) e.getCause();
            }
            throw e;
        }

        return toUnload;
    }
}
