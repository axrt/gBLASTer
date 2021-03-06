package sequence.nucleotide.genome;

import format.text.LargeFormat;

import java.io.*;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public class LargeChromosome extends Chromosome implements AutoCloseable{

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

    public InputStream getSequenceInputstream() {
        return this.sequence;
    }

    public static LargeChromosome fromRecord(InputStream inputStream, LargeFormat largeFormat) throws IOException {
        final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, 10000);
        int zero = 10000;
        bufferedInputStream.mark(zero);
        if (!largeFormat.checkFormatting(bufferedInputStream)) {
            throw new IllegalArgumentException("Bad record format!");
        }
        bufferedInputStream.reset();
        final String ac = largeFormat.getAc(bufferedInputStream);
        bufferedInputStream.reset();
        byte buffer[] = new byte[1];
        while (bufferedInputStream.read(buffer) > -1) {
            if (buffer[0] == (byte) '\n') {
                break;
            }
        }
        return new LargeChromosome(ac, bufferedInputStream, largeFormat);
    }

    public static LargeChromosome formPreprocessedComponents(String ac, InputStream sequence, LargeFormat format){
        return new LargeChromosome(ac, sequence,format);
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p>
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     * <p>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p>
     * <p>Note that unlike the {@link java.io.Closeable#close close}
     * method of {@link java.io.Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        this.sequence.close();
    }
}
