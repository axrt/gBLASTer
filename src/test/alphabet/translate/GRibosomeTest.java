package test.alphabet.translate;

import alphabet.character.amino.AminoAcid;
import alphabet.translate.GRibosome;
import alphabet.translate.GeneticCode;
import junit.extensions.TestSetup;
import org.junit.Test;
import sequence.nucleotide.NucleotideSequence;
import sequence.protein.ORF;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/5/14.
 * TODO document class
 */
public class GRibosomeTest {

    @Test
    public void test(){

        final Random random=new Random();
        final String[]nucleotides={"T","C","A","G"};
        StringBuilder randomMatrixBuilder=new StringBuilder();

        //for(int i=0;i<5000;i++){
            //randomMatrixBuilder.append(nucleotides[random.nextInt(4)]);
        //}
       //process(randomMatrixBuilder.toString());


        final int iters=100000;
        randomMatrixBuilder=new StringBuilder();
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
        }
        Date prStart;
        Date prStop;
        String result;

        final GRibosome ribosome = GRibosome.newInstance(NucleotideSequence.get(randomMatrixBuilder.toString(),"test"), GeneticCode.STANDARD);

        prStart=new Date();
        wasteParallel(ribosome,100);
        prStop=new Date();
        System.out.println("Time wasting parallel with "+iters+" genetic code iterations: "+benchmark(prStart,prStop).getTime()+" mils.");

        prStart=new Date();
        waste(ribosome,100);
        prStop=new Date();
        System.out.println("Time wasting singular with "+iters+" genetic code iterations: "+benchmark(prStart,prStop).getTime()+" mils.");

       /* prStart=new Date();
        final String resultParallel = processParallel(randomMatrixBuilder.toString());
        prStop=new Date();
        System.out.println("Time processing parallel with "+iters+" genetic code iterations: "+benchmark(prStart,prStop).getTime()+" mils.");

        prStart=new Date();
        result = process(randomMatrixBuilder.toString());
        prStop=new Date();
        System.out.println("Time processing singular with "+iters+" genetic code iterations: "+benchmark(prStart,prStop).getTime()+" mils.");*/



        //TestSetup.assertEquals(result,resultParallel);
    }
    private String process(String matrix){
        final GRibosome ribosome= GRibosome.newInstance(NucleotideSequence.get(matrix,"test"), GeneticCode.STANDARD);
        return ribosome.translate().map(ORF::toString).collect(Collectors.joining("\n"));
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

    /**
     * Created by alext on 6/5/14.
     * TODO document class
     */
    public static class GeneticCodeTest {

        @Test
        public void test(){

            final Map<String,AminoAcid> genteicCode=GeneticCode.STANDARD;

            final String expectedResult="FFLLLLLLIIIMVVVVSSSSPPPPTTTTAAAAYYXXHHQQNNKKDDEECCXWRRRRSSRRGGGG";

            final String[]rotator={"T","C","A","G"};
            StringBuilder testBuilder=new StringBuilder(64);
            for(String first:rotator){
                for(String second:rotator){
                    for(String third:rotator){
                        testBuilder.append(second);
                        testBuilder.append(first);
                        testBuilder.append(third);
                    }
                }
            }
            //System.out.println(testBuilder.toString());
            final String testMatrix=testBuilder.toString();
            testBuilder=new StringBuilder(64);
            for(int i=0;i<testMatrix.length();i+=3){
                final String codon=testMatrix.substring(i, i + 3);
                testBuilder.append(genteicCode.get(codon).getPillar());
            }
            System.out.println(testBuilder.toString());
            TestSetup.assertEquals(expectedResult,testBuilder.toString());
        }
    }
}
