package blast.blast;

import blast.output.BlastOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Created by alext on 5/27/14.
 * TODO document class
 */
public abstract class AbstractBlast<E> implements Callable<Optional<BlastOutput>> {
    protected List<BlastEventListner<E>> listeners;

    protected AbstractBlast() {
        this.listeners = new ArrayList<>();
    }

    public static class BlastEvent<E> {
        private final E cause;

        public BlastEvent(E event) {
            this.cause = event;
        }

        public Optional<E> getEvent() {
            return Optional.ofNullable(cause);
        }
    }

    public static interface BlastEventListner<E> {
        public int listen(BlastEvent<E> event);
    }

    public abstract int addListener(BlastEventListner<E> listner);

    public abstract int removeListener(BlastEventListner<E> listner);

    public abstract int notifyListeners(BlastEvent<E> event);
}
