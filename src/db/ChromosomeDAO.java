package db;

import format.text.LargeFormat;
import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.LargeChromosome;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by alext on 6/9/14.
 * TODO document class
 */
public interface ChromosomeDAO {

    public int saveChromososmeForGenomeID(int genomeId, Chromosome chromosome) throws Exception;
    public int saveLargeChromososmeForGenomeID(int genomeId, LargeChromosome largeChromosome) throws Exception;
    public Optional<Chromosome> loadCrhomosomeForID(int id)throws Exception;
    public Optional<LargeChromosome> loadLargeCrhomosomeForID(int id,LargeFormat largeFormat)throws Exception;
    public IntStream loadChromosomeIdsForGenomeId(int genomeId)throws Exception;

    /**
     * Note that the connection should be set to {@code false} autocommit if the database supports transactions
     * @param genomeId
     * @param chromoStream
     * @return
     * @throws Exception
     */
    public IntStream saveLargeChromosomesForGenomeId(int genomeId, Stream<? extends LargeChromosome> chromoStream, int counter) throws Exception;
    public Stream<LargeChromosome> loadLargeChromosomesForGemomeID(int genomeId, LargeFormat largeFormat) throws Exception;

}
