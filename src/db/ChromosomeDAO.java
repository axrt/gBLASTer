package db;

import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.LargeChromosome;

import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by alext on 6/9/14.
 * TODO document class
 */
public interface ChromosomeDAO {

    public int saveChromososmeForGenomeID(int genomeId, Chromosome chromosome) throws Exception;
    public int saveLargeChromososmeForGenomeID(int genomeId, LargeChromosome largeChromosome) throws Exception;
    public Chromosome loadCrhomosomeForID(int id)throws Exception;
    public LargeChromosome loadLargeCrhomosomeForID(int id)throws Exception;
    public IntStream loadChromosomeIdsForGenomeId(int genomeID)throws Exception;

}
