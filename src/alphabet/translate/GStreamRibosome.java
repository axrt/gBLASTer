package alphabet.translate;

import alphabet.character.amino.AminoAcid;
import alphabet.character.nucleotide.Nucleotide;
import sequence.protein.ORF;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 * Created by alext on 6/5/14.
 * TODO document class
 */
public class GStreamRibosome extends Ribosome<Nucleotide, AminoAcid, ORF> {

    protected final InputStream inputStream;
    protected final int   bufferLength;

    public GStreamRibosome(InputStream inputStream, int bufferLength) {
        super(null);
        this.inputStream=inputStream;
        this.bufferLength=bufferLength;
    }

    @Override
    public Stream<ORF> translate() throws IOException {

        return null;
    }
}
