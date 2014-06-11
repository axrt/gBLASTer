package db;

import format.text.LargeFormat;
import org.junit.Test;
import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.Genome;
import sequence.nucleotide.genome.LargeChromosome;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public class GenomeDAOTest {

    @Test
    public void test() {

        final Path toFastaRecord = Paths.get("/home/alext/Documents/Ocular Project/sequencing/DES/Fasta/1173L.fasta");
        GenomeDAO genomeDAO=new GenomeDAO() {
            private int counter=0;
            @Override
            public int saveGenomeForName(String name, Genome<? extends Chromosome> genome) throws Exception {
                return 1;
            }

            @Override
            public boolean genomeForNameExists(String name) throws Exception {
                return false;
            }

            @Override
            public int saveChromososmesForGenomeID(int genomeId, Chromosome chromosome) throws Exception {


                return 0;
            }

            @Override
            public int saveLargeChromososmesForGenomeID(int genomeId, LargeChromosome largeChromosome) throws Exception {

                try(FileOutputStream fileOutputStream=new FileOutputStream(new File(String.valueOf(counter).concat("test.chro")))) {
                   fileOutputStream.write(largeChromosome.getSequence().getBytes());
                }

                return 1;
            }

            @Override
            public Chromosome loadCrhomosomeForID(int id) throws Exception {
                return null;
            }
        };

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
