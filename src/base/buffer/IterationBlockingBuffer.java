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

    protected String name="";

    protected IterationBlockingBuffer(int capacity) {
        super(capacity);
    }
    protected IterationBlockingBuffer(String name,int capacity) {
        super(capacity); this.name=name;
    }

    @Override
    public int listen(AbstractBlast.BlastEvent<Iteration> event) throws InterruptedException {
        if (event.getEvent().isPresent()) {
            this.put(event.getEvent().get());
            if(this.remainingCapacity()==0){
                synchronized (System.out.getClass()) {
                    System.out.println("buffer ".concat(this.name) + " is full;");
                }
            }
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
    public static IterationBlockingBuffer get(String name,int capasity) {
        return new IterationBlockingBuffer(name,capasity);
    }
}
