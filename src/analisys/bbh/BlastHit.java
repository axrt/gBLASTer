package analisys.bbh;

import blast.output.Iteration;

/**
 * Created by alext on 6/19/14.
 * TODO document class
 */
public class BlastHit {

    protected final int id_blasts;
    protected final int orfs_id;
    protected final int hitorf_id;
    protected final Iteration iteration;

    protected BlastHit(int id_blasts, int orfs_id, int hitorf_id, Iteration iteration) {
        this.id_blasts = id_blasts;
        this.orfs_id = orfs_id;
        this.hitorf_id = hitorf_id;
        this.iteration = iteration;
    }

    public int getId_blasts() {
        return id_blasts;
    }

    public int getOrfs_id() {
        return orfs_id;
    }

    public int getHitorf_id() {
        return hitorf_id;
    }

    public Iteration getIteration() {
        return iteration;
    }

    public static BlastHit get(int id_blasts, int orfs_id, int hitorf_id, Iteration iteration){
        return new BlastHit(id_blasts,orfs_id,hitorf_id,iteration);
    }
}
