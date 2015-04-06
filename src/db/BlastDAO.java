package db;

import analisys.bbh.BidirectionalBlastHit;
import analisys.bbh.UnidirectionalBlastHit;
import blast.ncbi.output.Iteration;
import properties.jaxb.Genome;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by alext on 6/17/14.
 * TODO document class
 */
public interface BlastDAO {

    public int saveBlastResult(Iteration iteration, int qgenome_id, int tgenome_id) throws Exception;

    public int saveBitsScore(Iteration iteration, long id_blasts) throws Exception;

    public boolean genomeHasBeenBlastedOver(Genome query, Genome target) throws Exception;

    public Stream<BidirectionalBlastHit> getBBHforGenomePair(Genome one, Genome two, int balancer) throws Exception;

    public boolean saveBlastResultBatch(Stream<Iteration> iterations, int qgenome_id, int tgenome_id) throws Exception;

    public Stream<UnidirectionalBlastHit> getBHforGenomePair(Genome one, Genome two, double cutoff, int balancer) throws Exception;
}