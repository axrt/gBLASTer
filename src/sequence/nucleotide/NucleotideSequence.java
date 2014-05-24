package sequence.nucleotide;

import alphabet.character.nucleotide.Nucleotide;
import sequence.Sequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class NucleotideSequence<T extends Nucleotide> extends Sequence<T> {

    protected NucleotideSequence(String sequence, String ac) {
        super(sequence, ac);
    }


    public static NucleotideSequence<Nucleotide> get(String sequence, String ac){
        return new NucleotideSequence<>(sequence,ac);
    }

    @Override
    public boolean init() {
        if(!this.hasBeenInitialized){
            synchronized (this.characters){
                if(!this.hasBeenInitialized) {
                    this.characters = new ArrayList<>(this.sequence.length());
                    this.hasBeenInitialized = true;
                }
            }
        }
        return this.hasBeenInitialized;
    }
}
