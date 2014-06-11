package sequence.nucleotide.genome;

import format.text.LargeFormat;

import java.io.*;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public class LargeChromosome extends Chromosome {

    private final InputStream sequence;
    private final LargeFormat format;

    protected LargeChromosome(String ac, InputStream sequence, LargeFormat format) {
        super("", ac, format);
        this.sequence = sequence;
        this.format = format;
    }

    @Override
    public String getSequence() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.sequence))) {
            return bufferedReader.lines().collect(Collectors.joining());

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static LargeChromosome fromRecord(InputStream inputStream, LargeFormat largeFormat) throws Exception {
        final BufferedInputStream bufferedInputStream=new BufferedInputStream(inputStream,1000);
        int zero= 1000;
        bufferedInputStream.mark(zero);
        if (!largeFormat.checkFormatting(bufferedInputStream)) {
            throw new IllegalArgumentException("Bad record format!");
        }
        bufferedInputStream.reset();
        final String ac = largeFormat.getAc(bufferedInputStream);
        bufferedInputStream.reset();
        byte buffer[]=new byte[1];
        while(bufferedInputStream.read(buffer)>-1){
            if(buffer[0]==(byte)'\n'){
                break;
            }
        }
        bufferedInputStream.mark(zero);
        return new LargeChromosome(ac, bufferedInputStream, largeFormat);
    }
}
