package alphabet.character;

/**
 * Created by alext on 5/23/14.
 * TODO document class
 */
public abstract class Character {

    protected final char pillar;
    protected final byte representation;

    protected Character(char pillar, byte representation) {
        this.pillar = pillar;
        this.representation = representation;
    }

    public char getPillar() {
        return pillar;
    }

    public byte getRepresentation() {
        return representation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Character) {
            final Character c = (Character) obj;
            if (this.pillar == c.pillar && this.representation == c.representation) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.pillar + this.representation;
    }
}
