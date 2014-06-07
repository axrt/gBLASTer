package test.alphabet.translate;

import alphabet.character.amino.AminoAcid;
import alphabet.translate.GeneticCode;
import junit.extensions.TestSetup;
import org.junit.Test;

import java.util.Map;

/**
 * Created by alext on 6/6/14.
 * TODO document class
 */
public class GeneticCodeTest {

    @Test
    public void test() {

        final Map<String, AminoAcid> genteicCode = GeneticCode.STANDARD;

        final String expectedResult = "FFLLLLLLIIIMVVVVSSSSPPPPTTTTAAAAYYXXHHQQNNKKDDEECCXWRRRRSSRRGGGG";

        final String[] rotator = {"T", "C", "A", "G"};
        StringBuilder testBuilder = new StringBuilder(64);
        for (String first : rotator) {
            for (String second : rotator) {
                for (String third : rotator) {
                    testBuilder.append(second);
                    testBuilder.append(first);
                    testBuilder.append(third);
                }
            }
        }
        //System.out.println(testBuilder.toString());
        final String testMatrix = testBuilder.toString();
        testBuilder = new StringBuilder(64);
        for (int i = 0; i < testMatrix.length(); i += 3) {
            final String codon = testMatrix.substring(i, i + 3);
            testBuilder.append(genteicCode.get(codon).getPillar());
        }
        System.out.println(testBuilder.toString());
        TestSetup.assertEquals(expectedResult, testBuilder.toString());
    }
}

