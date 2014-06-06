package alphabet.translate;

import alphabet.character.*;
import alphabet.character.Character;
import alphabet.character.amino.AminoAcid;
import alphabet.protein.AminoAcidAlphabet;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alext on 6/5/14.
 * TODO document class
 */
public class GeneticCode<A extends AminoAcid> extends HashMap<String,A> {
    public static final GeneticCode<AminoAcid> STANDARD=new GeneticCode<>(64,"Standard Genetic Code");
    static {
        //TXT
        STANDARD.put("TTT", AminoAcidAlphabet.ALPHABET.F.getAA());
        STANDARD.put("TTC", AminoAcidAlphabet.ALPHABET.F.getAA());
        STANDARD.put("TTA", AminoAcidAlphabet.ALPHABET.L.getAA());
        STANDARD.put("TTG", AminoAcidAlphabet.ALPHABET.L.getAA());
        //CXT
        STANDARD.put("CTT", AminoAcidAlphabet.ALPHABET.L.getAA());
        STANDARD.put("CTC", AminoAcidAlphabet.ALPHABET.L.getAA());
        STANDARD.put("CTA", AminoAcidAlphabet.ALPHABET.L.getAA());
        STANDARD.put("CTG", AminoAcidAlphabet.ALPHABET.L.getAA());
        //AXT
        STANDARD.put("ATT", AminoAcidAlphabet.ALPHABET.I.getAA());
        STANDARD.put("ATC", AminoAcidAlphabet.ALPHABET.I.getAA());
        STANDARD.put("ATA", AminoAcidAlphabet.ALPHABET.I.getAA());
        STANDARD.put("ATG", AminoAcidAlphabet.ALPHABET.M.getAA());
        //GXT
        STANDARD.put("GTT", AminoAcidAlphabet.ALPHABET.V.getAA());
        STANDARD.put("GTC", AminoAcidAlphabet.ALPHABET.V.getAA());
        STANDARD.put("GTA", AminoAcidAlphabet.ALPHABET.V.getAA());
        STANDARD.put("GTG", AminoAcidAlphabet.ALPHABET.V.getAA());
        //TXC
        STANDARD.put("TCT", AminoAcidAlphabet.ALPHABET.S.getAA());
        STANDARD.put("TCC", AminoAcidAlphabet.ALPHABET.S.getAA());
        STANDARD.put("TCA", AminoAcidAlphabet.ALPHABET.S.getAA());
        STANDARD.put("TCG", AminoAcidAlphabet.ALPHABET.S.getAA());
        //CXC
        STANDARD.put("CCT", AminoAcidAlphabet.ALPHABET.P.getAA());
        STANDARD.put("CCC", AminoAcidAlphabet.ALPHABET.P.getAA());
        STANDARD.put("CCA", AminoAcidAlphabet.ALPHABET.P.getAA());
        STANDARD.put("CCG", AminoAcidAlphabet.ALPHABET.P.getAA());
        //AXC
        STANDARD.put("ACT", AminoAcidAlphabet.ALPHABET.T.getAA());
        STANDARD.put("ACC", AminoAcidAlphabet.ALPHABET.T.getAA());
        STANDARD.put("ACA", AminoAcidAlphabet.ALPHABET.T.getAA());
        STANDARD.put("ACG", AminoAcidAlphabet.ALPHABET.T.getAA());
        //GXC
        STANDARD.put("GCT", AminoAcidAlphabet.ALPHABET.A.getAA());
        STANDARD.put("GCC", AminoAcidAlphabet.ALPHABET.A.getAA());
        STANDARD.put("GCA", AminoAcidAlphabet.ALPHABET.A.getAA());
        STANDARD.put("GCG", AminoAcidAlphabet.ALPHABET.A.getAA());
        //TXA
        STANDARD.put("TAT", AminoAcidAlphabet.ALPHABET.Y.getAA());
        STANDARD.put("TAC", AminoAcidAlphabet.ALPHABET.Y.getAA());
        STANDARD.put("TAA", AminoAcidAlphabet.ALPHABET.STOP.getAA());
        STANDARD.put("TAG", AminoAcidAlphabet.ALPHABET.STOP.getAA());
        //CXA
        STANDARD.put("CAT", AminoAcidAlphabet.ALPHABET.H.getAA());
        STANDARD.put("CAC", AminoAcidAlphabet.ALPHABET.H.getAA());
        STANDARD.put("CAA", AminoAcidAlphabet.ALPHABET.Q.getAA());
        STANDARD.put("CAG", AminoAcidAlphabet.ALPHABET.Q.getAA());
        //AXA
        STANDARD.put("AAT", AminoAcidAlphabet.ALPHABET.N.getAA());
        STANDARD.put("AAC", AminoAcidAlphabet.ALPHABET.N.getAA());
        STANDARD.put("AAA", AminoAcidAlphabet.ALPHABET.K.getAA());
        STANDARD.put("AAG", AminoAcidAlphabet.ALPHABET.K.getAA());
        //GXA
        STANDARD.put("GAT", AminoAcidAlphabet.ALPHABET.D.getAA());
        STANDARD.put("GAC", AminoAcidAlphabet.ALPHABET.D.getAA());
        STANDARD.put("GAA", AminoAcidAlphabet.ALPHABET.E.getAA());
        STANDARD.put("GAG", AminoAcidAlphabet.ALPHABET.E.getAA());
        //TXG
        STANDARD.put("TGT", AminoAcidAlphabet.ALPHABET.C.getAA());
        STANDARD.put("TGC", AminoAcidAlphabet.ALPHABET.C.getAA());
        STANDARD.put("TGA", AminoAcidAlphabet.ALPHABET.STOP.getAA());
        STANDARD.put("TGG", AminoAcidAlphabet.ALPHABET.W.getAA());
        //CXG
        STANDARD.put("CGT", AminoAcidAlphabet.ALPHABET.R.getAA());
        STANDARD.put("CGC", AminoAcidAlphabet.ALPHABET.R.getAA());
        STANDARD.put("CGA", AminoAcidAlphabet.ALPHABET.R.getAA());
        STANDARD.put("CGG", AminoAcidAlphabet.ALPHABET.R.getAA());
        //AXG
        STANDARD.put("AGT", AminoAcidAlphabet.ALPHABET.S.getAA());
        STANDARD.put("AGC", AminoAcidAlphabet.ALPHABET.S.getAA());
        STANDARD.put("AGA", AminoAcidAlphabet.ALPHABET.R.getAA());
        STANDARD.put("AGG", AminoAcidAlphabet.ALPHABET.R.getAA());
        //GXG
        STANDARD.put("GGT", AminoAcidAlphabet.ALPHABET.G.getAA());
        STANDARD.put("GGC", AminoAcidAlphabet.ALPHABET.G.getAA());
        STANDARD.put("GGA", AminoAcidAlphabet.ALPHABET.G.getAA());
        STANDARD.put("GGG", AminoAcidAlphabet.ALPHABET.G.getAA());
    }
    protected final String name;

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *                                            or the load factor is nonpositive
     */
    public GeneticCode(int initialCapacity, float loadFactor, String name) {
        super(initialCapacity, loadFactor);
        this.name = name;
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public GeneticCode(int initialCapacity, String name) {
        super(initialCapacity);
        this.name = name;
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public GeneticCode(String name) {
        this.name = name;
    }

    /**
     * Constructs a new <tt>HashMap</tt> with the same mappings as the
     * specified <tt>Map</tt>.  The <tt>HashMap</tt> is created with
     * default load factor (0.75) and an initial capacity sufficient to
     * hold the mappings in the specified <tt>Map</tt>.
     *
     * @param m the map whose mappings are to be placed in this map
     * @throws NullPointerException if the specified map is null
     */
    public GeneticCode(Map<? extends String, ? extends A> m, String name) {
        super(m);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
