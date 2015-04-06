package format.binary.nucleotide;

import alphabet.Alphabet;
import alphabet.character.nucleotide.Nucleotide;
import alphabet.nucleotide.NucleotideAlphabet;
import format.binary.BinarySequenceRepresentation;
import sequence.nucleotide.NucleotideSequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class NucleotideBinarySequenceRepresentation extends BinarySequenceRepresentation<Nucleotide, NucleotideSequence<Nucleotide>> {

    public static int AC_SIZE = 256;//32 bytes
    protected final Alphabet<Nucleotide> nucleotideAlphabet;

    protected NucleotideBinarySequenceRepresentation(Builder builder) {
        super(builder.sequences, builder.header, builder.stop);
        this.nucleotideAlphabet = builder.nucleotideAlphabet;
    }

    @Override
    public byte[] convert() {

        final List<byte[]> sequenceRepresentations = this.sequences.stream().map(sequence -> nucleotideAlphabet.encode(sequence.getSequence())).collect(Collectors.toList());
        //Allocate byte array and fill in
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            //Add header and number of sequences
            byteArrayOutputStream.write(this.header);
            byteArrayOutputStream.write(ByteBuffer.allocate(4).putInt(sequenceRepresentations.size()).array());
            //For each sequence add positions to the header
            final int hl = this.header.length + 4;
            int current = hl + (sequenceRepresentations.size() + 1) * 4;//First byte of the positions
            for (int i = 0; i < sequenceRepresentations.size(); i++) {
                byteArrayOutputStream.write(ByteBuffer.allocate(4).putInt(current).array());
                current += sequenceRepresentations.get(i).length + 33;//33 cuz 256 bits==32 bytes for the AC and 1 byte for the stop
            }
            byteArrayOutputStream.write(ByteBuffer.allocate(4).putInt(current).array()); //Writes the coordinate of the last stop
            //Put each representation with it's AC and sequence
            for (int i = 0; i < sequenceRepresentations.size(); i++) {
                byteArrayOutputStream.write(ByteBuffer.allocate(32).put(this.sequences.get(i).getAc().getBytes()).array());
                byteArrayOutputStream.write(sequenceRepresentations.get(i));
                byteArrayOutputStream.write(this.stop);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {

        private List<NucleotideSequence<Nucleotide>> sequences;
        private byte[] header;
        private byte[] stop;
        private Alphabet<Nucleotide> nucleotideAlphabet;

        public Builder() {
            this.nucleotideAlphabet = NucleotideAlphabet.get();
        }

        public Builder sequences(List<NucleotideSequence<Nucleotide>> sequences) {
            this.sequences = sequences;
            return this;
        }

        public Builder header(byte[] header) {
            this.header = header;
            return this;
        }

        public Builder stop(byte[] stop) {
            this.stop = stop;
            return this;
        }

        public Builder alphabet(Alphabet<Nucleotide> nucleotideAlphabet) {
            this.nucleotideAlphabet = nucleotideAlphabet;
            return this;
        }

        public NucleotideBinarySequenceRepresentation build() {
            if (this.sequences == null || this.header == null || this.stop == null) {
                throw new IllegalStateException("Sequences, header and record stop must be set prior to build! Default alphabet is Nucletide.");
            }
            return new NucleotideBinarySequenceRepresentation(this);
        }
    }
}
