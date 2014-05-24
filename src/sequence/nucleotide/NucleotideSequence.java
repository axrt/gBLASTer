package sequence.nucleotide;

import alphabet.character.nucleotide.Nucleotide;
import sequence.Sequence;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class NucleotideSequence<T extends Nucleotide> extends Sequence<T> {

    public NucleotideSequence(String sequence, String ac) {
        super(sequence, ac);
    }
}
