package test.alphabet.translate;

import alphabet.character.amino.AminoAcid;
import alphabet.character.nucleotide.Nucleotide;
import alphabet.translate.GRibosome;
import alphabet.translate.GStreamRibosome;
import alphabet.translate.GeneticCode;
import alphabet.translate.Ribosome;
import junit.extensions.TestSetup;
import org.junit.Test;
import sequence.nucleotide.NucleotideSequence;
import sequence.protein.ORF;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/5/14.
 * TODO document class
 */
public class GRibosomeTest {

    //@Test
    public void test(){

        final Random random=new Random();
        final String[]nucleotides={"T","C","A","G"};
        StringBuilder randomMatrixBuilder=new StringBuilder();
        final int iters=5003;
        for(int i=0;i<iters;i++){
           randomMatrixBuilder.append(nucleotides[random.nextInt(4)]);
        }


       //process(randomMatrixBuilder.toString());



        /*randomMatrixBuilder=new StringBuilder();
        for(int i=0;i<iters;i++) {
            for (String first : nucleotides) {
                for (String second : nucleotides) {
                    for (String third : nucleotides) {
                        randomMatrixBuilder.append(second);
                        randomMatrixBuilder.append(first);
                        randomMatrixBuilder.append(third);
                    }
                }
            }
        }*/
        Date prStart;
        Date prStop;
        String resultSequence=null;
        String resultInputsteam=null;

        final NucleotideSequence<Nucleotide> nucleotideNucleotideSequence=NucleotideSequence.get(randomMatrixBuilder.toString(), "test");
        final GRibosome ribosome = GRibosome.newInstance(nucleotideNucleotideSequence, GeneticCode.STANDARD);
        final GStreamRibosome gStreamRibosome=GStreamRibosome.newInstance(new ByteArrayInputStream(randomMatrixBuilder.toString().getBytes(StandardCharsets.UTF_8)),GeneticCode.STANDARD);

        /*prStart=new Date();
        wasteParallel(ribosome,100);
        prStop=new Date();
        System.out.println("Time wasting parallel with "+iters+" genetic code iterations: "+benchmark(prStart,prStop).getTime()+" mils.");

        prStart=new Date();
        waste(ribosome,100);
        prStop=new Date();
        System.out.println("Time wasting singular with "+iters+" genetic code iterations: "+benchmark(prStart,prStop).getTime()+" mils.");

        prStart=new Date();
        final String resultParallel = processParallel(randomMatrixBuilder.toString());
        prStop=new Date();
        System.out.println("Time processing parallel with "+iters+" genetic code iterations: "+benchmark(prStart,prStop).getTime()+" mils.");
        */

        try {
            prStart=new Date();
            resultSequence = process(ribosome);
            prStop=new Date();
            System.out.println("Time processing ribosome with "+iters+" genetic code iterations: "+benchmark(prStart,prStop).getTime()+" mils.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            prStart=new Date();
            resultInputsteam = process(gStreamRibosome);
            prStop=new Date();
            System.out.println("Time processing setream-based ribosome with "+iters+" genetic code iterations: "+benchmark(prStart,prStop).getTime()+" mils.");

        } catch (Exception e) {
            e.printStackTrace();
        }


        TestSetup.assertEquals(resultSequence,resultInputsteam); //This test will not pass due to that the Ribosome has the actual STOP orf returned, and the other does not
    }
    private String process(Ribosome<Nucleotide, AminoAcid, ORF> ribosome) throws Exception {
        return ribosome.translate().sorted(Comparator.comparing(o->o.getSequence())).map(ORF::getSequence).collect(Collectors.joining("\n"));
    }
    private void waste(GRibosome ribosome,int times){
        for(int i=0;i<times;i++) {
            ribosome.translate().forEach(ORF::getAc);
        }
    }
    //private String processParallel(String matrix){
      //  final GRibosome ribosome= GRibosome.newInstance(NucleotideSequence.get(matrix,"test"), GeneticCode.STANDARD);
        //return ribosome.translateParallel().map(ORF::toString).collect(Collectors.joining("\n"));
    //}
    private void wasteParallel(GRibosome ribosome,int times){
        for(int i=0;i<times;i++) {
            ribosome.translateParallel().forEach(ORF::getAc);
        }
    }
    public Date benchmark(Date start,Date stop){
        return new Date(stop.getTime()-start.getTime());
    }
    public static void main(String[]args){
        new GRibosomeTest().test();
    }

    @Test
    public void realTest(){
       try(BufferedReader bufferedReader=new BufferedReader(new FileReader(new File("/home/alext/Downloads/arabidopsis.txt")));
       InputStream inputStream =new FileInputStream(new File("/home/alext/Downloads/arabidopsis.txt"))){
           final NucleotideSequence<Nucleotide> nucleotideNucleotideSequence=NucleotideSequence.get(bufferedReader.lines().skip(1).collect(Collectors.joining()), "test");
           final GRibosome ribosome = GRibosome.newInstance(nucleotideNucleotideSequence, GeneticCode.STANDARD);
           final GStreamRibosome gStreamRibosome=GStreamRibosome.newInstance(inputStream,GeneticCode.STANDARD);
          System.out.println(ribosome.translate().filter(orf->orf.getSequence().length()>50).filter(orf->!orf.getSequence().contains("STOP")).count());
           //System.out.println(gStreamRibosome.translate().count());
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
    }
}
