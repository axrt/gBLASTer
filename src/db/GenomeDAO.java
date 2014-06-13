package db;

import alphabet.character.amino.AminoAcid;
import format.text.LargeFormat;
import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.Genome;
import sequence.nucleotide.genome.LargeGenome;

import java.util.Map;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public interface GenomeDAO extends ChromosomeDAO{

    public int saveGenomeForName(Genome<? extends Chromosome> genome) throws Exception;
    public int saveLargeGenome(LargeGenome genome) throws Exception;
    public boolean genomeForNameExists(String name)throws Exception;
    public long deployAndTranslateLargeGenome(LargeGenome genome,LargeFormat largeFormat,Map<String,AminoAcid> geneticCode) throws Exception;

}
