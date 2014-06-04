package sequence.protein;

import alphabet.character.amino.AminoAcid;
import sequence.Sequence;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public class ProteinSequence<T extends AminoAcid> extends Sequence<T> {

    protected ProteinSequence(String sequence, String ac) {
        super(sequence, ac);
    }

    public static ProteinSequence<AminoAcid> get(String sequence, String ac) {

        return new ProteinSequence<>(sequence, ac);
    }
}
