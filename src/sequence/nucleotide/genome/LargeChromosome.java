package sequence.nucleotide.genome;

import format.text.LargeFormat;

import java.io.*;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public class LargeChromosome extends Chromosome {

    private final InputStream record;
    private final LargeFormat format;
    protected LargeChromosome(String ac, InputStream record,LargeFormat format) {
        super(ac, "",format);
        this.record=record;
        this.format=format;
    }

    @Override
    public String getSequence() {
        try {
            try(BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(this.record))){
                return bufferedReader.lines().skip(1).collect(Collectors.joining());
            }
        } catch (IOException e) {

        } finally {
            return "";
        }
    }

    public static LargeChromosome fromRecord(InputStream inputStream,LargeFormat largeFormat) throws Exception {
        if(!largeFormat.checkFormatting(inputStream)){
          throw new IllegalArgumentException("Bad record format!");
        }
        final String ac=largeFormat.getAc(inputStream);
        return new LargeChromosome(ac, inputStream,largeFormat);
    }
}
