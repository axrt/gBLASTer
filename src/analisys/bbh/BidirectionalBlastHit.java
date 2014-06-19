package analisys.bbh;

import blast.output.Iteration;

/**
 * Created by alext on 6/19/14.
 * TODO document class
 */
public class BidirectionalBlastHit {

    protected final BlastHit forwardHit;
    protected final BlastHit reverseHit;

    public BidirectionalBlastHit(BlastHit forwardHit, BlastHit reverseHit) {
        this.forwardHit = forwardHit;
        this.reverseHit = reverseHit;
    }

    public BlastHit getForwardHit() {
        return forwardHit;
    }

    public BlastHit getReverseHit() {
        return reverseHit;
    }
}
