package test.gblaster.blast;

import base.buffer.SimpleBlockingBuffer;
import blast.blast.AbstractBlast;
import blast.output.BlastOutput;
import blast.output.Iteration;

import gblaster.blast.GBlast;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Created by alext on 5/28/14.
 * TODO document class
 */
public class GBlastNTest {

    @Test
    public void testGBlastN() {

        final ExecutorService executor = Executors.newFixedThreadPool(2);

        final Path toBlastN = Paths.get("/bin/blastn");
        final Path toTestFile = Paths.get("/home/alext/Documents/tuit/final testing/blast.test.fasta");
        final String db = "16SMicrobial";

        final int bufferCapasity = 1;
        final IterationEventBuffer buffer=new IterationEventBuffer(bufferCapasity);

        executor.execute(buffer);
        final AbstractBlast.BlastBuilder builder = new GBlast.GBlastNBuilder(toBlastN, toTestFile, db).num_threads(Optional.of(12));
        final GBlast gblastn =((GBlast.GBlastNBuilder)builder).build();
        gblastn.addListener(buffer);
        final Future<Optional<BlastOutput>> future = executor.submit(gblastn);
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        buffer.release();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //executor.shutdown();


    }

    private static class IterationEventBuffer extends SimpleBlockingBuffer<Iteration> implements AbstractBlast.BlastEventListner<Iteration>, Runnable {
        private IterationEventBuffer(int capacity) {
            super(capacity);
        }

        @Override
        public int listen(AbstractBlast.BlastEvent<Iteration> event) {
            event.getEvent().ifPresent(i->{
                try{this.put(i);}catch (InterruptedException ie){ie.printStackTrace();}
            });
            return this.size();
        }

        @Override
        public synchronized boolean isDone() {

            if (this.isEmpty() && this.done == true) {
                return true;
            }
            return false;
        }

        @Override
        public synchronized void release() {
            this.done = true;
            notifyAll();
        }

        @Override
        public void run() {
             final Thread t=new Thread(this);
            while(!isDone()){
                final Iteration i;
                try {
                    i = this.take();
                    System.out.println(i.getIterationHits().getHit().size());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}
