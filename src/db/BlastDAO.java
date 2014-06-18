package db;

import blast.output.Iteration;

/**
 * Created by alext on 6/17/14.
 * TODO document class
 */
public interface BlastDAO  {

    public int saveBlastResult(Iteration iteration) throws Exception;

}