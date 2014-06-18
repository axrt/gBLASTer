package base.buffer;

import blast.blast.AbstractBlast;
import blast.output.Iteration;

import java.util.concurrent.TimeUnit;

/**
 * Created by alext on 6/17/14.
 * TODO document class
 */
public class IterationBlockingBuffer extends SimpleBlockingBuffer<Iteration> implements AbstractBlast.BlastEventListner<Iteration> {

    public static final Iteration DONE=new Iteration();

    protected IterationBlockingBuffer(int capacity) {
        super(capacity);
    }

    @Override
    public int listen(AbstractBlast.BlastEvent<Iteration> event) throws InterruptedException {
        if (event.getEvent().isPresent()) {
            this.put(event.getEvent().get());
            return 0;
        }
        return -1;
    }

    @Override
    public synchronized boolean isDone() {

        if (this.isEmpty() && this.done == true) {
            return true;
        }
        return false;
    }

    @Override
    public void release() throws InterruptedException {
        synchronized (this){
            this.done = true;
        }
        this.put(DONE);
    }

    public static IterationBlockingBuffer get(int capasity) {
        return new IterationBlockingBuffer(capasity);
    }
}
