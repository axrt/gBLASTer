package alphabet.character.amino;

/**
 * Created by alext on 5/28/14.
 * TODO document class
 */
public class AminoAcid extends alphabet.character.Character {

    protected final String threeLetterRep;

    public AminoAcid(char pillar, byte representation, String threeLetterRep) {
        super(pillar, representation);
        this.threeLetterRep = threeLetterRep;
    }

    public String getThreeLetterRep() {
        return this.threeLetterRep;
    }
}
