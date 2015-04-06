package sequence.nucleotide;

import alphabet.character.nucleotide.Nucleotide;
import sequence.Sequence;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class NucleotideSequence<T extends Nucleotide> extends Sequence<T> {

    protected NucleotideSequence(String sequence, String ac) {
        super(sequence, ac);
    }


    public static NucleotideSequence<Nucleotide> get(String sequence, String ac) {

        return new NucleotideSequence<>(sequence, ac);
    }

}
