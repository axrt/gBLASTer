package sequence.protein;

import alphabet.character.amino.AminoAcid;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public class ORF extends ProteinSequence<AminoAcid> {
    private int id;
    private final int start;
    private final int stop;
    private final int frame;

    protected ORF(String sequence, String ac, int start, int stop, int frame) {
        super(sequence, ac);
        this.start = start;
        this.stop = stop;
        this.frame = frame;
        this.id=0;
    }

    public int getFrame() {
        return frame;
    }

    public int getStart() {
        return start;
    }

    public int getStop() {
        return stop;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("AC: ");
        stringBuilder.append(this.ac);
        stringBuilder.append('\n');
        stringBuilder.append("frame: ");
        stringBuilder.append(this.frame);
        stringBuilder.append('\n');
        stringBuilder.append("start: ");
        stringBuilder.append(this.start);
        stringBuilder.append('\n');
        stringBuilder.append("stop: ");
        stringBuilder.append(this.stop);
        stringBuilder.append('\n');
        stringBuilder.append(this.sequence);
        return stringBuilder.toString();
    }

    public static ORF get(String sequence, String ac, int start, int stop, int frame){
        return new ORF(sequence,ac,start,stop,frame);
    }
}
