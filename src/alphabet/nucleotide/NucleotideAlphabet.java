package alphabet.nucleotide;

import alphabet.Alphabet;
import alphabet.character.nucleotide.Nucleotide;
import sequence.Sequence;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public final class NucleotideAlphabet extends Alphabet<Nucleotide> {

    public static enum ALPHABET {
        A(new Nucleotide('A', (byte) 0b10001000)), //A
        G(new Nucleotide('G', (byte) 0b01001000)), //G
        C(new Nucleotide('C', (byte) 0b00101000)), //C
        T(new Nucleotide('T', (byte) 0b00011000)), //T

        R(new Nucleotide('R', (byte) 0b11000000)), //A or G
        M(new Nucleotide('M', (byte) 0b10100000)), //A or C
        W(new Nucleotide('W', (byte) 0b10010000)), //A or T
        S(new Nucleotide('S', (byte) 0b01100000)), //G or C
        K(new Nucleotide('K', (byte) 0b01010000)), //G or T
        Y(new Nucleotide('Y', (byte) 0b00110000)), //C or T

        V(new Nucleotide('V', (byte) 0b11100000)), //A or G or C
        H(new Nucleotide('H', (byte) 0b10110000)), //A or C or T
        D(new Nucleotide('D', (byte) 0b11010000)), //A or G or T
        B(new Nucleotide('B', (byte) 0b01110000)), //G or C or T
        N(new Nucleotide('N', (byte) 0b11110000)), //A or G or C or T

        GAP(new Nucleotide('-', (byte) 0b00000100)), //Alignment gap (–)
        Q(new Nucleotide('?', (byte) 0b00000010)), //Unknown character (?)
        ;

        private final Nucleotide nucleotide;

        public Nucleotide getNucleotide() {
            return nucleotide;
        }

        private static Map<Character, Nucleotide> byPillar = new HashMap<>();

        static {
            for (ALPHABET a : ALPHABET.values()) {
                byPillar.put(a.nucleotide.getPillar(), a.nucleotide);  //Hashmap turned out to be faster in tests against switch statement
            }
        }

        private static Map<Byte, Nucleotide> byRepresentation = new HashMap<>();

        static {
            for (ALPHABET a : ALPHABET.values()) {
                byRepresentation.put(a.nucleotide.getRepresentation(), a.nucleotide);
            }
        }

        ALPHABET(Nucleotide nucleotide) {
            this.nucleotide = nucleotide;
        }

        public static Optional<Nucleotide> getByPillar(char pillar) {
            final Optional<Nucleotide> o = Optional.ofNullable(byPillar.get(Character.toUpperCase(pillar)));
            o.orElseThrow(() -> new IllegalArgumentException("Invalid nucleotide character: ".concat(String.valueOf(pillar))));
            return o;
        }

        public static Optional<Nucleotide> getByRepresentation(byte representation) {
            final Optional<Nucleotide> o = Optional.ofNullable(byRepresentation.get(representation));
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

    public char rc(char letter) {
        letter = Character.toUpperCase(letter);
        switch (letter) {
            case 'A':
                return ALPHABET.T.nucleotide.getPillar();
            case 'C':
                return ALPHABET.G.nucleotide.getPillar();
            case 'G':
                return ALPHABET.C.nucleotide.getPillar();
            case 'T':
                return ALPHABET.A.nucleotide.getPillar();

            case 'R':
                return ALPHABET.Y.nucleotide.getPillar();
            case 'M':
                return ALPHABET.K.nucleotide.getPillar();
            case 'W':
                return ALPHABET.S.nucleotide.getPillar();

            case 'Y':
                return ALPHABET.R.nucleotide.getPillar();
            case 'K':
                return ALPHABET.M.nucleotide.getPillar();
            case 'S':
                return ALPHABET.W.nucleotide.getPillar();

            case 'N':
                return ALPHABET.N.nucleotide.getPillar();

            default:
                return '0';
            //throw new IllegalArgumentException("Cannot get a reverse complement for the given character " + letter + '!');
        }
    }

    public char rc(byte representation) {
        return this.rc(this.toPillar(representation));
    }

    public byte rcByte(char letter) {
        return this.toRepresentaton(this.rc(letter));
    }

    public byte rcByte(byte representation) {
        switch (representation) {

            case (byte) 0b10001000:
                return ALPHABET.T.nucleotide.getRepresentation();
            case (byte) 0b01001000:
                return ALPHABET.C.nucleotide.getRepresentation();
            case (byte) 0b00101000:
                return ALPHABET.G.nucleotide.getRepresentation();
            case (byte) 0b00011000:
                return ALPHABET.A.nucleotide.getRepresentation();

            case (byte) 0b11000000:
                return ALPHABET.Y.nucleotide.getRepresentation();
            case (byte) 0b10100000:
                return ALPHABET.K.nucleotide.getRepresentation();
            case (byte) 0b10010000:
                return ALPHABET.S.nucleotide.getRepresentation();

            case (byte) 0b00110000:
                return ALPHABET.R.nucleotide.getRepresentation();
            case (byte) 0b01010000:
                return ALPHABET.M.nucleotide.getRepresentation();
            case (byte) 0b01100000:
                return ALPHABET.W.nucleotide.getRepresentation();

            case (byte) 0b011110000:
                return ALPHABET.W.nucleotide.getRepresentation();

            default:
                throw new IllegalArgumentException("Cannot get a reverse complement for the given character " + this.toPillar(representation) + '!');
        }
    }

    public byte[] rcByteArray(String sequence) {
        final byte[] representation = new byte[sequence.length()];
        for (int i = 0; i < representation.length; i++) {
            representation[i] = rcByte(sequence.charAt(i));
        }
        return representation;
    }

    public byte[] rcByteArray(Sequence<Nucleotide> nucleotideSequence) {
        return rcByteArray(nucleotideSequence.getSequence());
    }

    public byte[] rcByteArray(byte[] representations) {
        for (int i = 0; i < representations.length; i++) {
            representations[i] = rcByte(rcByte(representations[i]));
        }
        return representations;
    }

    public String rcString(String sequence) {
        final char[] pillars = new char[sequence.length()];
        for (int i = 0; i < pillars.length; i++) {
            pillars[i] = rc(sequence.charAt(i));
        }
        return new String(pillars);
    }

    public String rcString(Sequence<Nucleotide> nucleotideSequence) {
        return rcString(nucleotideSequence.getSequence());
    }

    public String rcString(byte[] representations) {
        final char[] pillars = new char[representations.length];
        for (int i = 0; i < pillars.length; i++) {
            pillars[i] = rc(representations[i]);
        }
        return new String(pillars);
    }

    private static class LazyHolder {
        private static final NucleotideAlphabet INSTANCE = new NucleotideAlphabet();
    }

    public static NucleotideAlphabet get() {
        return LazyHolder.INSTANCE;
    }
}
