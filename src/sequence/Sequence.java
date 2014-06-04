package sequence;

import alphabet.character.Character;
import alphabet.character.nucleotide.Nucleotide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public abstract class Sequence<T extends Character>{

    protected final String sequence;
    protected final String ac;

    protected Sequence(String sequence, String ac) {
        this.sequence = sequence;
        this.ac = ac;
    }

    public String getAc() {
        return ac;
    }

    @Override
    public String toString() {
        return this.sequence.toString();
    }

    public String getSequence() {
        return sequence;
    }


}
