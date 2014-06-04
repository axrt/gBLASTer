package alphabet.translate;

import alphabet.character.amino.AminoAcid;
import alphabet.character.nucleotide.Nucleotide;
import alphabet.protein.AminoAcidAlphabet;
import sequence.Sequence;
import sequence.protein.ORF;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public class GRibosome extends Ribosome<Nucleotide, AminoAcid> {

    protected final Map<String, AminoAcid> codonTable;

    public GRibosome(Sequence<Nucleotide> matrix, Map<String, AminoAcid> codonTable) {
        super(matrix);
        this.codonTable = codonTable;
    }

    @Override
    public Stream<Sequence<AminoAcid>> translate() {
        return null;
    }

    protected class Frame {

        private final String matrixString;
        private final int frame;

        public Frame(String matrixString, int frame) {
            this.matrixString = matrixString;
            this.frame=frame;
        }

        protected Stream<Sequence<AminoAcid>> run() {


            Iterator<Sequence<AminoAcid>> iter = new Iterator<Sequence<AminoAcid>>() {
                int position = 0;
                int count = 0;

                @Override
                public boolean hasNext() {
                    if (position < matrixString.length() - 6) {
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                public Sequence<AminoAcid> next() {
                    final StringBuilder stringBuilder = new StringBuilder();
                    int start = position;
                    for (; position < matrixString.length() - 6; position += 3) {
                        final AminoAcid nextAA = codonTable.get(matrixString.substring(position, position + 3));
                        if (nextAA == null || nextAA.getPillar() == AminoAcidAlphabet.ALPHABET.STOP.getAA().getPillar()) {
                            break;
                        }
                        stringBuilder.append(nextAA.getPillar());
                    }
                    count++;
                    return ORF.get(stringBuilder.toString(),String.valueOf(count),start,position,frame);

                }
            };
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
        }

    }
}
