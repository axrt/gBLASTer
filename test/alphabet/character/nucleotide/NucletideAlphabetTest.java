package alphabet.character.nucleotide;

import alphabet.Alphabet;
import alphabet.character.nucleotide.Nucleotide;
import alphabet.nucleotide.NucleotideAlphabet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class NucletideAlphabetTest {
    private final static Alphabet<Nucleotide> NUCLEOTIDE_ALPHABET = NucleotideAlphabet.get();

    @Test
    public void testEncode() {

        final String testString = "ATGCatgc-?NRMWSKYVHDBnrmwskyvhdb";
        System.out.println(testString);
        final byte[] representations = new byte[testString.length()];
        for (int i = 0; i < testString.length(); i++) {
            representations[i] = NUCLEOTIDE_ALPHABET.toRepresentaton(testString.charAt(i));
        }
        for (int i = 0; i < representations.length; i++) {
            Assert.assertTrue(Character.toUpperCase(testString.charAt(i)) == NUCLEOTIDE_ALPHABET.toPillar(representations[i]));
            System.out.print(NUCLEOTIDE_ALPHABET.toPillar(representations[i]));
        }
        Assert.assertArrayEquals(NUCLEOTIDE_ALPHABET.encode(testString), representations);
        Assert.assertEquals(testString.toUpperCase(), NUCLEOTIDE_ALPHABET.decode(representations));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnexistingCharacter() {
        NUCLEOTIDE_ALPHABET.toRepresentaton('j');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnexistingByteRepresentation() {
        NUCLEOTIDE_ALPHABET.toPillar((byte) 0b1111111);
    }

    @Test
    public void testRC() {
        final String testString = "ATGCRMWSKYN";
        final NucleotideAlphabet alph = NucleotideAlphabet.get();
        System.out.println("RC: " + alph.rcString(testString));
        Assert.assertEquals(testString, alph.rcString(alph.rcString(testString)));
        Assert.assertEquals(testString, alph.rcString(alph.rcString(alph.rcString(alph.rcByteArray(testString)))));
        Assert.assertEquals(testString, alph.rcString(alph.rcString(alph.rcByteArray(alph.rcString(testString)))));
    }

    @Test
    public void testPerformance() {
        final int numberOfIteratons = 10000000;
        final NucleotideAlphabet alph = NucleotideAlphabet.get();
        final NucleotideAlphabet.ALPHABET[] values = NucleotideAlphabet.ALPHABET.values();
        final int valuesSize = NucleotideAlphabet.ALPHABET.values().length;
        final Random r = new Random();

        final HashMap<Character, Nucleotide> charAccess = new HashMap();
        for (NucleotideAlphabet.ALPHABET a : values) {
            charAccess.put(a.getNucleotide().getPillar(), a.getNucleotide());
        }
        System.out.println("Testing HasMap performance:");
        Date start = new Date();
        for (int i = 0; i < numberOfIteratons; i++) {
            charAccess.get(values[r.nextInt(valuesSize)].getNucleotide().getPillar());
        }
        Date stop = new Date();
        System.out.println("Test took " + new Date(stop.getTime() - start.getTime()).getTime() + " mils.");

        System.out.println("Testing switch performance:");
        start = new Date();
        for (int i = 0; i < numberOfIteratons; i++) {
            NucleotideAlphabet.ALPHABET.getByPillar(values[r.nextInt(valuesSize)].getNucleotide().getPillar());
        }
        stop = new Date();
        System.out.println("Test took " + new Date(stop.getTime() - start.getTime()).getTime() + " mils.");


        /*start=new Date();
        for(int i=0;i<numberOfIteratons;i++){
            NucleotideAlphabet.ALPHABET.getRCCharacter(values[r.nextInt(valuesSize)].getNucleotide().getPillar());
        }
        stop=new Date();
        */
        System.out.println("Test took " + new Date(stop.getTime() - start.getTime()).getTime() + " mils.");

        start = new Date();
        for (int i = 0; i < numberOfIteratons; i++) {
            alph.rc(values[r.nextInt(valuesSize)].getNucleotide().getPillar());
        }
        stop = new Date();
        System.out.println("Test took " + new Date(stop.getTime() - start.getTime()).getTime() + " mils.");
    }
}


