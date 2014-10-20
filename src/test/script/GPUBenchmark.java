package test.script;

import blast.output.BlastOutput;
import format.text.CommonFormats;
import gblaster.blast.GPUGBlast;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by alext on 10/20/14.
 * TODO document class
 */
public class GPUBenchmark {
    private static char[] AMINO = new char[]{'A', 'R', 'N', 'D', 'C', 'E', 'Q', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V'};
    private static final Path exec = Paths.get("/usr/local/bin/gmakeblastdb");
    private static final Path outputDir=Paths.get("/home/alext/Documents/GPU/titan_human_test");
    private static final Path outputDB=outputDir.resolve("randb.fasta");
    private static final Path outputTestSet=outputDir.resolve("randtest.fasta");
    private static final int numberOfSequences = 5000;
    private static final int minLength = 100;
    private static final int maxLength = 3000;
    private static final int numThreads = 6;
    private static final Random random = new Random();

    public static void main(String[] args) {

       final ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
       final List<Future<String>> futures=new LinkedList<>();


       try(BufferedWriter bufferedWriter =new BufferedWriter(new FileWriter(outputDB.toFile()));
           BufferedWriter bufferedWriter2 =new BufferedWriter(new FileWriter(outputTestSet.toFile()))){
           final Listener listener=new Listener() {
               @Override
               public synchronized void listen(final String s) throws IOException {
                   bufferedWriter.write(s);
                   bufferedWriter.newLine();
               }
           };
           final Listener listener2=new Listener() {
               @Override
               public synchronized void listen(final String s) throws IOException {
                   bufferedWriter2.write(s);
                   bufferedWriter2.newLine();
               }
           };
           for(int i=0;i<numberOfSequences;i++){
               final Generator dbGenerator=generator("DB_"+i,minLength,maxLength);
               dbGenerator.setListener(listener);
               futures.add(executorService.submit(dbGenerator));
           }
           for(int i=0;i<numberOfSequences/10;i++){
               final Generator testSet=generator("TEST_"+i,minLength,maxLength);
               testSet.setListener(listener2);
               futures.add(executorService.submit(testSet));
           }
           for(Future f:futures){
               f.get();
           }


       } catch (IOException e) {
           e.printStackTrace();
       } catch (InterruptedException e) {
           e.printStackTrace();
       } catch (ExecutionException e) {
           e.printStackTrace();
       }
        executorService.shutdown();

    }
    private interface Listener{
        public void listen(String s) throws IOException;
    }
    private interface Generator extends Callable<String>{
        public void setListener(Listener listener);
    }
    public static Generator generator(final String ac,final int minLength, final int maxLength) {

        return new Generator() {

            private Listener listener;

            @Override
            public String call() throws Exception {
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(CommonFormats.Fasta.FASTA_START);
                stringBuilder.append(ac);
                stringBuilder.append('\n');
                final Random R = new Random();
                final int length=R.nextInt(maxLength)+minLength;
                for (int i = 0; i < length; i++) {
                    stringBuilder.append(AMINO[R.nextInt(AMINO.length)]);
                }
                this.notifyListener(stringBuilder.toString());
                return stringBuilder.toString();
            }
            @Override
            public void setListener(Listener listener){
                this.listener=listener;
            }
            private void notifyListener(String s) throws IOException {
                this.listener.listen(s);
            }
        };
    }

    public static class GPUBLASTTester extends GPUGBlast {
        protected GPUBLASTTester(GPUGBlastBuilder builder) {
            super(builder);
        }

        @Override
        public Optional<BlastOutput> call() throws Exception {
            return super.call();
        }
    }

}
