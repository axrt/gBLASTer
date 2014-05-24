package test.alphabet.character.nucleotide;

import alphabet.Alphabet;
import alphabet.character.nucleotide.Nucleotide;
import alphabet.nucleotide.NucleotideAlphabet;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public class NucletideAlphabetTest {
    private final static Alphabet<Nucleotide> NUCLEOTIDE_ALPHABET = NucleotideAlphabet.get();

    @Test
    public void testEncode(){

        final String testString="ATGCatgc-?NRMWSKYVHDBnrmwskyvhdb";
        System.out.println(testString);
        final byte[]representations=new byte[testString.length()];
        for(int i=0;i<testString.length();i++){
            representations[i]= NUCLEOTIDE_ALPHABET.toRepresentaton(testString.charAt(i));
        }
        for(int i=0;i<representations.length;i++){
            Assert.assertTrue(Character.toUpperCase(testString.charAt(i))==NUCLEOTIDE_ALPHABET.toPillar(representations[i]));
            System.out.print(NUCLEOTIDE_ALPHABET.toPillar(representations[i]));
        }
        Assert.assertArrayEquals(NUCLEOTIDE_ALPHABET.encode(testString),representations);
        Assert.assertEquals(testString.toUpperCase(),NUCLEOTIDE_ALPHABET.decode(representations));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnexistingCharacter(){
         NUCLEOTIDE_ALPHABET.toRepresentaton('j');
    }
    @Test(expected = IllegalArgumentException.class)
    public void testUnexistingByteRepresentation(){
        NUCLEOTIDE_ALPHABET.toPillar((byte)0b1111111);
    }
}
