package format.binary;

import alphabet.character.Character;
import sequence.Sequence;
import format.SequenceRepresentation;

import java.util.List;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public abstract class BinarySequenceRepresentation<T extends Character,S extends Sequence<T>> extends SequenceRepresentation<T,S>{

    protected final byte[] header;
    protected final byte[] stop;

    protected BinarySequenceRepresentation(List<S> sequences, byte[] header, byte[] stop) {
        super(sequences);
        this.header = header;
        this.stop = stop;
    }
}
