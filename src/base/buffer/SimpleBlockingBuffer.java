package base.buffer;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public abstract class SimpleBlockingBuffer<E> extends ArrayBlockingQueue<E> {

    protected boolean done;

    public SimpleBlockingBuffer(int capacity) {
        super(capacity);
        this.done=false;
    }

    public SimpleBlockingBuffer(int capacity, boolean fair) {
        super(capacity, fair);
        this.done=false;
    }

    public SimpleBlockingBuffer(int capacity, boolean fair, Collection<? extends E> c) {
        super(capacity, fair, c);
        this.done=false;
    }

    public synchronized boolean isDone(){
        return this.done;
    }

    /**
     * Should allow to finish the job and set boolean done to true
     */
    public abstract void release() throws InterruptedException;
}
