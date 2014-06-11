package db;

import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.LargeChromosome;

import java.io.InputStream;

/**
 * Created by alext on 6/9/14.
 * TODO document class
 */
public interface ChromosomeDAO {

    public int saveChromososmesForGenomeID(int genomeId, Chromosome chromosome) throws Exception;
    public int saveLargeChromososmesForGenomeID(int genomeId, LargeChromosome largeChromosome) throws Exception;
    public Chromosome loadCrhomosomeForID(int id)throws Exception;

}
