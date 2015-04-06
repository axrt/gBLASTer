package alphabet.translate;

import alphabet.character.amino.AminoAcid;
import alphabet.character.nucleotide.Nucleotide;
import alphabet.protein.AminoAcidAlphabet;
import sequence.Sequence;
import sequence.protein.ORF;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public class GRibosome extends Ribosome<Nucleotide, AminoAcid, ORF> {

    protected final Map<String, AminoAcid> codonTable;

    protected GRibosome(Sequence<Nucleotide> matrix, Map<String, AminoAcid> codonTable) {
        super(matrix);
        this.codonTable = codonTable;
    }

    @Override
    public Stream<ORF> translate() {

        return Stream.of(
                new Frame(this.matrix.getSequence(), 0),
                new Frame(this.matrix.getSequence(), 1),
                new Frame(this.matrix.getSequence(), 2)
        ).flatMap(frame -> frame.run());
    }

    public Stream<ORF> translateParallel() {

        return this.translate().parallel();
    }

    protected class Frame {

        private final String matrixString;
        private final int frame;

        public Frame(String matrixString, int frame) {
            this.matrixString = matrixString.substring(frame);
            this.frame = frame;
        }

        protected Stream<ORF> run() {


            Iterator<ORF> iter = new Iterator<ORF>() {
                int position = 0;
                int count = 0;

                @Override
                public boolean hasNext() {
                    if (position < matrixString.length()) {
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                public ORF next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final StringBuilder stringBuilder = new StringBuilder();
                    int start = position;
                    for (; position < matrixString.length() - 2; position += 3) {
                        final AminoAcid nextAA = codonTable.get(matrixString.substring(position, position + 3));
                        if (nextAA == null || nextAA.getPillar() == AminoAcidAlphabet.ALPHABET.STOP.getAA().getPillar()) {

                            if (stringBuilder.length() == 0) {
                                stringBuilder.append(AminoAcidAlphabet.ALPHABET.STOP.toString());
                            }
                            break;
                        }
                        stringBuilder.append(nextAA.getPillar());
                    }
                    count++;
                    final ORF orf = ORF.get(stringBuilder.toString(), String.valueOf(count), start, position, frame);
                    position += 3;
                    //System.out.println("new orf was born");
                    return orf;
                }
            };
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iter, Spliterator.NONNULL), false);
        }

    }

    public static GRibosome newInstance(Sequence<Nucleotide> matrix, Map<String, AminoAcid> codonTable) {
        return new GRibosome(matrix, codonTable);
    }
}
