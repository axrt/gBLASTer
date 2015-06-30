package db;

import analisys.bbh.TripledirectionalBlastHit;
import properties.jaxb.Genome;

import java.util.stream.Stream;

/**
 * Created by alext on 6/30/15. Some weired functions for custom research stuff
 */
public interface ResearchDAO {

   public Stream<TripledirectionalBlastHit> getTBHForGenomes(Genome a, Genome b, Genome c, int balancer) throws Exception;

}
