package db;

import blast.output.Iteration;
import properties.jaxb.Genome;

/**
 * Created by alext on 6/17/14.
 * TODO document class
 */
public interface BlastDAO  {

    public int saveBlastResult(Iteration iteration) throws Exception;
    public boolean genomeHasBeenBlastedOver(Genome query,Genome target) throws Exception;
}