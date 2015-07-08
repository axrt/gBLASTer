package db;

import db.legend.GenomeLegend;
import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.Genome;
import sequence.nucleotide.genome.LargeGenome;

import java.util.List;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public interface GenomeDAO extends ChromosomeDAO {

    public int saveGenomeForName(Genome<? extends Chromosome> genome) throws Exception;

    public int saveGenomeForName(String genome) throws Exception;

    public int saveLargeGenome(LargeGenome genome) throws Exception;

    public boolean genomeForNameExists(String name) throws Exception;

    public int genomeIdByName(String name) throws Exception;

    public boolean removeGenomeForName(String name) throws Exception;

    public  List<GenomeLegend.GenomeLegendLine> getLegend() throws  Exception;
}
