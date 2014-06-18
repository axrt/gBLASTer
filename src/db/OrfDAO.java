package db;

import sequence.protein.ORF;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by alext on 6/13/14.
 * TODO document class
 */
public interface OrfDAO {

    public IntStream saveOrfsForChromosomeId(int idChromosome,Stream<? extends ORF> orfStream, int batchSize) throws Exception;
    public Stream<ORF> loadAllOrfsForGenomeId(int genomeId,int balancer,int minLength,int maxLength) throws Exception;
}
