package sequence.nucleotide.genome;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public class Genome<C extends Chromosome> extends ArrayList<C> {

    protected final String name;

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public Genome(String name) {
        this.name = name;
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public Genome(int initialCapacity, String name) {
        super(initialCapacity);
        this.name = name;
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public Genome(Collection<? extends C> c, String name) {
        super(c);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
