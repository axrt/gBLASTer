package alphabet.translate;

import alphabet.character.amino.AminoAcid;
import alphabet.character.nucleotide.Nucleotide;
import alphabet.protein.AminoAcidAlphabet;
import sequence.protein.ORF;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by alext on 6/5/14.
 * TODO document class
 */
public class GStreamRibosome extends Ribosome<Nucleotide, AminoAcid, ORF> {

    protected final InputStream inputStream;
    protected final Map<String, AminoAcid> codonTable;
    protected final Queue<ORF> orfs;

    protected GStreamRibosome(InputStream inputStream, Map<String, AminoAcid> codonTable) {
        super(null);
        this.inputStream = inputStream;
        this.codonTable = codonTable;
        this.orfs = new LinkedList<>();
    }

    @Override
    public Stream<ORF> translate() throws IOException {
        try (final DataInputStream dataInputStream = new DataInputStream(inputStream)) {
            //Initialization to emulate the frame shift
            final Frame[] frames = {new Frame(0), new Frame(1), new Frame(2)};
            try{
                char n = dataInputStream.readChar();
                frames[0].accept(n);
                n= dataInputStream.readChar();
                frames[0].accept(n);
                frames[1].accept(n);
            }catch (EOFException e){

                return Stream.empty();
            }

            //If here - can proceed
            Iterator<ORF> iter = new Iterator<ORF>() {
                boolean streamIsEmpty = false;

                private void read() {
                    try {
                        char nu;
                        try {
                            while (true) {
                                nu = dataInputStream.readChar();
                                if (nu == '\n') next(); //skip new lines

                                final boolean atLeastOne = (frames[0].accept(nu) | frames[1].accept(nu) | frames[2].accept(nu));
                                if (atLeastOne) {
                                    break;
                                }
                            }
                        } catch (EOFException e) {
                            streamIsEmpty = true;
                            for (Frame f : frames) {
                                f.finalizeORF();
                            }
                        }
                    } catch (IOException e) {
                        streamIsEmpty = true;
                    }
                }

                @Override
                public boolean hasNext() {

                    if (!orfs.isEmpty() || !streamIsEmpty) {
                        return true;
                    } else return false;

                }

                @Override
                public ORF next() {
                    if (orfs.size()>0) {
                        final ORF orf = orfs.poll();
                        //System.out.println(orf);
                        return orf;
                    }
                    if (hasNext()) {
                        read();
                        final ORF orf = orfs.poll();
                        if (orf == null) throw new RuntimeException("Could not find a single ORF, please check input!");
                        return orf;
                    } else throw new NoSuchElementException();
                }
            };
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iter, Spliterator.NONNULL), false);
        }
    }

    protected class Frame {

        protected final char[] triplet;
        protected final int frame;

        protected StringBuilder orfBuilder;
        protected int pointer;
        protected int orfStart;
        protected int orfStop;

        protected int orfsCreated;

        protected Frame(int frame) {
            this.triplet = new char[3];
            this.orfBuilder = new StringBuilder();
            this.frame = frame;
        }

        protected boolean accept(char nucleotide) {

            this.triplet[this.pointer] = nucleotide;
            this.pointer++;
            if (this.pointer > 2) {
                this.pointer = 0;
                final String codon = new String(this.triplet);
                final AminoAcid aa = codonTable.get(codon); //TODO think of a reusable container for this, StringBuilder seems quite obvious, but the genetic table has to be redesigned in this case
                if (aa == null || aa.getPillar() == AminoAcidAlphabet.ALPHABET.STOP.getAA().getPillar()) {

                    if (this.orfBuilder.length() == 0) {
                        return false;
                    } else {
                        this.finalizeORF();
                        return true;
                    }

                } else {
                    this.orfBuilder.append(aa.getPillar());
                    this.orfStop += 3;
                }
            }
            return false;
        }

        protected void finalizeORF() {
            final ORF orf = ORF.get(this.orfBuilder.toString(), String.valueOf(this.orfsCreated), this.orfStart, this.orfStop, this.frame);
            orfs.add(orf);
            this.orfsCreated++;
            this.orfBuilder = new StringBuilder();
            this.orfStart = this.orfStop + 1;
        }
    }

    public static GStreamRibosome newInstance(InputStream inputStream, Map<String, AminoAcid> codonTable) {
        return new GStreamRibosome(inputStream, codonTable);
    }
}