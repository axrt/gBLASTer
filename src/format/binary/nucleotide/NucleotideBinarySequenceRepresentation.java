package format.binary.nucleotide;

import alphabet.Alphabet;
import alphabet.character.nucleotide.Nucleotide;
import sequence.nucleotide.NucleotideSequence;
import format.binary.BinarySequenceRepresentation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class NucleotideBinarySequenceRepresentation extends BinarySequenceRepresentation<Nucleotide,NucleotideSequence<Nucleotide>>{

    public static int AC_SIZE=256;//32 bytes
    protected final Alphabet<Nucleotide> nucleotideAlphabet;

    protected NucleotideBinarySequenceRepresentation(Builder builder) {
        super(builder.sequences, builder.header, builder.stop);
        this.nucleotideAlphabet = builder.nucleotideAlphabet;
    }

    @Override
    public byte[] convert() {

        final List<byte[]> sequenceRepresentations = this.sequences.stream().map(sequence->nucleotideAlphabet.encode(sequence.getSequence())).collect(Collectors.toList());

        final List<byte[]> representationList=new ArrayList<>();

        //Add header and number of sequences
        representationList.add(this.header);
        representationList.add(ByteBuffer.allocate(4).putInt(sequenceRepresentations.size()).array());

        //For each sequence add positions to the header
        final int hl=this.header.length+4;
        final ByteBuffer byteBuffer=ByteBuffer.allocate(sequenceRepresentations.size()*4);
        int current=hl;//First byte of the positions
        for(int i=0;i<sequenceRepresentations.size();i++){
            byteBuffer.putInt(current);
            current+=sequenceRepresentations.get(i).length+33;//33 cuz 256 bits==32 bytes for the AC and 1 byte for the stop
        }
        representationList.add(byteBuffer.array());

        //Put each representation with it's AC and sequence
        for(int i=0;i<sequenceRepresentations.size();i++){
            representationList.add(this.sequences.get(i).getAc().getBytes());
            representationList.add(sequenceRepresentations.get(i));
            representationList.add(this.stop);
        }

        //Allocate byte array and fill in
        final ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();

        representationList.stream().forEach(b -> {
            try {
                byteArrayOutputStream.write(b);
            } catch (IOException e){
              throw new RuntimeException(e);
            }
        });
        return byteArrayOutputStream.toByteArray();
    }

    public static class Builder{

        private List<NucleotideSequence<Nucleotide>> sequences;
        private byte[] header;
        private byte[] stop;
        private Alphabet<Nucleotide> nucleotideAlphabet;

        public Builder(){

        }

        public void sequences(List<NucleotideSequence<Nucleotide>> sequences) {
            this.sequences = sequences;
        }

        public void header(byte[] header) {
            this.header = header;
        }

        public void stop(byte[] stop) {
            this.stop = stop;
        }

        public void alphabet (Alphabet<Nucleotide> nucleotideAlphabet) {
            this.nucleotideAlphabet = nucleotideAlphabet;
        }

        public NucleotideBinarySequenceRepresentation build(){
            if(this.sequences==null||this.header==null||this.stop==null||this.nucleotideAlphabet==null){
                throw new IllegalStateException("Sequences, header, record stop and alphabet must be set prior to build!");
            }
            return new NucleotideBinarySequenceRepresentation(this);
        }
    }
}
