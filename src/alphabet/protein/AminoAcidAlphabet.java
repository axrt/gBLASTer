package alphabet.protein;

import alphabet.Alphabet;
import alphabet.character.amino.AminoAcid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public class AminoAcidAlphabet extends Alphabet<AminoAcid> {
    public static enum ALPHABET {

        G(new AminoAcid('G', (byte) 'G', "Gly")),
        A(new AminoAcid('A', (byte) 'A', "Ala")),
        V(new AminoAcid('V', (byte) 'V', "Val")),
        L(new AminoAcid('L', (byte) 'L', "Leu")),
        I(new AminoAcid('I', (byte) 'I', "Ile")),

        M(new AminoAcid('M', (byte) 'M', "Met")),
        F(new AminoAcid('F', (byte) 'F', "Phe")),
        P(new AminoAcid('P', (byte) 'P', "Pro")),
        S(new AminoAcid('S', (byte) 'S', "Ser")),
        T(new AminoAcid('T', (byte) 'T', "Thr")),

        C(new AminoAcid('C', (byte) 'C', "Cys")),
        Y(new AminoAcid('Y', (byte) 'Y', "Tyr")),
        N(new AminoAcid('N', (byte) 'N', "Asn")),
        Q(new AminoAcid('Q', (byte) 'Q', "Gln")),
        W(new AminoAcid('W', (byte) 'W', "Trp")),

        D(new AminoAcid('D', (byte) 'D', "Asp")),
        E(new AminoAcid('E', (byte) 'E', "Glu")),
        K(new AminoAcid('K', (byte) 'K', "Lys")),
        R(new AminoAcid('R', (byte) 'R', "Arg")),
        H(new AminoAcid('H', (byte) 'H', "His")),

        STOP(new AminoAcid('X', (byte) 'X', "Stop"));


        private final AminoAcid aminoAcid;

        public AminoAcid getAA() {
            return aminoAcid;
        }

        private static Map<Character, AminoAcid> byPillar = new HashMap<>();

        static {
            for (ALPHABET a : ALPHABET.values()) {
                byPillar.put(a.aminoAcid.getPillar(), a.aminoAcid);  //Hashmap turned out to be faster in tests against switch statement
            }
        }

        private static Map<Byte, AminoAcid> byRepresentation = new HashMap<>();

        static {
            for (ALPHABET a : ALPHABET.values()) {
                byRepresentation.put(a.aminoAcid.getRepresentation(), a.aminoAcid);
            }
        }

        ALPHABET(AminoAcid aminoAcid) {
            this.aminoAcid = aminoAcid;
        }

        public static Optional<AminoAcid> getByPillar(char pillar) {
            final Optional<AminoAcid> o = Optional.ofNullable(byPillar.get(Character.toUpperCase(pillar)));
            o.orElseThrow(() -> new IllegalArgumentException("Invalid nucleotide character: ".concat(String.valueOf(pillar))));
            return o;
        }

        public static Optional<AminoAcid> getByRepresentation(byte representation) {
            final Optional<AminoAcid> o = Optional.ofNullable(byRepresentation.get(representation));
            o.orElseThrow(() -> new IllegalArgumentException("No character representation for a given bytecode!"));
            return o;
        }
    }

    @Override
    public byte toRepresentaton(char pillar) {
        //Convert to uppercase to reduce redundancy in coding
        pillar = Character.toUpperCase(pillar);
        //Select byte representation to return
        return ALPHABET.getByPillar(pillar).get().getRepresentation();
    }

    @Override
    public char toPillar(byte representation) {
        return ALPHABET.getByRepresentation(representation).get().getPillar();
    }

    @Override
    public byte[] encode(String sequence) {
        final byte[] encoded = new byte[sequence.length()];
        for (int i = 0; i < sequence.length(); i++) {
            encoded[i] = this.toRepresentaton(sequence.charAt(i));
        }
        return encoded;
    }

    @Override
    public String decode(byte[] representations) {
        final char[] pillars = new char[representations.length];
        for (int i = 0; i < representations.length; i++) {
            pillars[i] = this.toPillar(representations[i]);
        }
        return new String(pillars);
    }
}
