package alphabet;

import alphabet.character.Character;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public abstract class Alphabet<C extends Character> {

     public abstract byte toRepresentaton(char pillar);
     public abstract char toPillar(byte representation);
     public abstract byte[]encode(String sequence);
     public abstract String decode(byte[]representations);


}
