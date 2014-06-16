package sequence.nucleotide.genome;

import format.text.LargeFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Created by alext on 6/11/14.
 * TODO document class
 */
public class LargeGenome extends Genome<LargeChromosome> {

    private final InputStream genome;
    private final LargeFormat largeFormat;
    private final Path tmpFolder;

    /**
     * Constructs an empty list with an initial capacity of ten.
     *
     * @param name
     */
    protected LargeGenome(String name, InputStream genome, LargeFormat largeFormat, Path tmpFolder) {
        super(name);
        this.genome = genome;
        this.largeFormat = largeFormat;
        this.tmpFolder = tmpFolder;
    }

    @Override
    public Stream<LargeChromosome> stream() {
        return this.largeFormat.iterateRecords(this.genome, tmpFolder.resolve(this.name.concat(".tmp.gen")))
                .map(is -> {
                    try {
                        return LargeChromosome.fromRecord(is, this.largeFormat);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    @Override
    public Stream<LargeChromosome> parallelStream() {
        return this.stream();
    }


    public static LargeGenome grasp(String name, InputStream genome, LargeFormat largeFormat, Path tmpFolder) {
        return new LargeGenome(name, genome, largeFormat, tmpFolder);
    }
}
