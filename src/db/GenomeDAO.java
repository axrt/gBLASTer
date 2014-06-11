package db;

import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.Genome;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public interface GenomeDAO extends ChromosomeDAO{

    public int saveGenomeForName(String name,Genome<? extends Chromosome> genome) throws Exception;
    public boolean genomeForNameExists(String name)throws Exception;

}
