package sequence.protein;

import alphabet.character.amino.AminoAcid;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public class ORF extends ProteinSequence<AminoAcid> {
    private final int start;
    private final int stop;
    private final int frame;

    protected ORF(String sequence, String ac, int start, int stop, int frame) {
        super(sequence, ac);
        this.start = start;
        this.stop = stop;
        this.frame = frame;
    }
    public static ORF get(String sequence, String ac, int start, int stop, int frame){
        return new ORF(sequence,ac,start,stop,frame);
    }
}
