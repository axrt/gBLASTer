package sequence.nucleotide.genome;

import alphabet.character.nucleotide.Nucleotide;
import format.text.Format;
import sequence.nucleotide.NucleotideSequence;
import sequence.protein.ORF;

import java.io.*;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public class Chromosome extends NucleotideSequence<Nucleotide> {

    private final Format format;
    protected Chromosome(String sequence, String ac,Format format) {
        super(sequence, ac);
        this.format=format;
    }
    public static Chromosome fromFile(Path toChromosomeFile, Format format) throws IOException {
     return fromInputStream(new FileInputStream(toChromosomeFile.toFile()),format);
    }
    public static Chromosome fromInputStream(InputStream inputStream, Format format) throws IOException {
        try(BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream))){
           final String record=bufferedReader.lines().collect(Collectors.joining("\n"));
           if(format.checkFormatting(record)){
               return new Chromosome(format.getAc(record),format.getSequence(record),format);
           } else throw new IllegalArgumentException("Format error!");
        }
    }
}
