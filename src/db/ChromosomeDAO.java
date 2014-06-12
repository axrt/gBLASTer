package db;

import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.LargeChromosome;

import java.io.IOException;

/**
 * Created by alext on 6/9/14.
 * TODO document class
 */
public interface ChromosomeDAO {

    public int saveChromososmeForGenomeID(int genomeId, Chromosome chromosome) throws IOException;
    public int saveLargeChromososmesForGenomeID(int genomeId, LargeChromosome largeChromosome) throws IOException;
    public Chromosome loadCrhomosomeForID(int id)throws IOException;
    public LargeChromosome loadLargeCrhomosomeForID(int id)throws IOException;
}
