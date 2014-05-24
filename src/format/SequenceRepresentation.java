package format;

import alphabet.character.Character;
import sequence.Sequence;

import java.util.List;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public abstract class SequenceRepresentation<T extends Character, S extends Sequence<T>> {

    protected final List<S> sequences;

    protected SequenceRepresentation(List<S> sequences) {
        this.sequences = sequences;
    }

    public abstract byte[] convert();


}
