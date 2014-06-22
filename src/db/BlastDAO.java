package db;

import analisys.bbh.BidirectionalBlastHit;
import blast.output.Iteration;
import properties.jaxb.Genome;

import java.util.stream.Stream;

/**
 * Created by alext on 6/17/14.
 * TODO document class
 */
public interface BlastDAO  {

    public int saveBlastResult(Iteration iteration) throws Exception;
    public boolean genomeHasBeenBlastedOver(Genome query,Genome target) throws Exception;
    public Stream<BidirectionalBlastHit> getBBHforGenomePair(Genome one,Genome two,int balancer) throws Exception;
    public void commit()throws Exception;
}