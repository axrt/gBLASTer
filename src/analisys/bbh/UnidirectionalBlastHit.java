package analisys.bbh;

/**
 * Created by alext on 9/9/14.
 * TODO document class
 */
public class UnidirectionalBlastHit extends BlastHit {

    protected final int rev_id_genomes;
    protected final double bitScoreCutoff;
    protected final String hitSequence;

    protected UnidirectionalBlastHit(int id_genomes, int rev_id_genomes, long id_blasts, long orfs_id, String sequence, long hitorf_id, String textIteration, double bitScoreCutoff, String hitSequence) {
        super(id_genomes, id_blasts, orfs_id, sequence, hitorf_id, textIteration);
        this.rev_id_genomes = rev_id_genomes;
        this.bitScoreCutoff = bitScoreCutoff;
        this.hitSequence = hitSequence;
    }

    public double getBitScoreCutoff() {
        return bitScoreCutoff;
    }

    public String getHitSequence() {
        return hitSequence;
    }

    public int getRev_id_genomes() {
        return rev_id_genomes;
    }

    public static UnidirectionalBlastHit get(int id_genomes, int rev_id_genomes,
                                             long id_blasts, long orfs_id,
                                             String sequence, long hitorf_id,
                                             String textIteration, double bitScoreCutoff,
                                             String hitSequence) {
        return new UnidirectionalBlastHit(id_genomes, rev_id_genomes, id_blasts, orfs_id, sequence,
                hitorf_id, textIteration, bitScoreCutoff, hitSequence);
    }
}
