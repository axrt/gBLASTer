package analisys.bbh;

/**
 * Created by alext on 6/30/15.
 */
public class TripledirectionalBlastHit {

    protected final BlastHit A;
    protected final BlastHit B;
    protected final BlastHit C;

    public TripledirectionalBlastHit(BlastHit a, BlastHit b, BlastHit c) {
        this.A = a;
        this.B = b;
        this.C = c;
    }

    public BlastHit getA() {
        return A;
    }

    public BlastHit getC() {
        return C;
    }

    public BlastHit getB() {
        return B;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(A.toString());
        stringBuilder.append("\t");
        stringBuilder.append(B.toString());
        stringBuilder.append("\t");
        stringBuilder.append(C.toString());
        return stringBuilder.toString();
    }
}
