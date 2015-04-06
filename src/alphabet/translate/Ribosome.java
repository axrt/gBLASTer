package alphabet.translate;

import alphabet.character.Character;
import sequence.Sequence;

import java.util.stream.Stream;


/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public abstract class Ribosome<N extends Character, A extends Character, S extends Sequence<A>> {

    protected final Sequence<N> matrix;

    protected Ribosome(Sequence<N> matrix) {
        this.matrix = matrix;
    }

    public abstract Stream<S> translate() throws Exception;

}
