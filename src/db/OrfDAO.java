package db;

import sequence.protein.ORF;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by alext on 6/13/14.
 * TODO document class
 */
public interface OrfDAO {

    public IntStream saveOrfsForChromosomeId(int idChromosome,Stream<? extends ORF> orfStream) throws Exception;
    public Stream<ORF> loadAllOrfsForGenomeId(int genomeId) throws Exception;
}
