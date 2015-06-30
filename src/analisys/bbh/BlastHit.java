package analisys.bbh;

import blast.blast.BlastHelper;
import blast.ncbi.output.Iteration;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.util.Optional;

/**
 * Created by alext on 6/19/14.
 * TODO document class
 */
public class BlastHit {

    protected final int id_genomes;
    protected final long id_blasts;
    protected final long orfs_id;
    protected final String sequence;
    protected final long hitorf_id;
    protected final String textIteration;

    /**
     *
     * @param id_genomes query genome
     * @param id_blasts
     * @param orfs_id
     * @param sequence
     * @param hitorf_id
     * @param textIteration
     */
    protected BlastHit(int id_genomes, long id_blasts, long orfs_id, String sequence, long hitorf_id, String textIteration) {
        this.id_genomes = id_genomes;
        this.id_blasts = id_blasts;
        this.orfs_id = orfs_id;
        this.sequence = sequence;
        this.hitorf_id = hitorf_id;
        this.textIteration = textIteration;
    }

    public long getId_blasts() {
        return id_blasts;
    }

    public long getOrfs_id() {
        return orfs_id;
    }

    public String getSequence() {
        return sequence;
    }

    public long getHitorf_id() {
        return hitorf_id;
    }

    public String getTextIteration() {
        return textIteration;
    }

    public int getId_genomes() {
        return id_genomes;
    }

    public Optional<Iteration> getIteration() throws JAXBException, SAXException {
        return BlastHelper.unmarshallSingleIteraton(new ByteArrayInputStream(textIteration.getBytes()));
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(this.id_genomes);
        stringBuilder.append("\t");
        stringBuilder.append(id_blasts);
        stringBuilder.append("\t");
        stringBuilder.append(orfs_id);
        stringBuilder.append("\t");
        stringBuilder.append(sequence);
        stringBuilder.append("\t");
        stringBuilder.append(hitorf_id);
        stringBuilder.append("\t");
        stringBuilder.append(textIteration);
        return stringBuilder.toString();
    }

    public static BlastHit get(int id_genomes, long id_blasts, long orfs_id, String sequence, long hitorf_id, Iteration iteration) throws JAXBException {
        return new BlastHit(id_genomes, id_blasts, orfs_id, sequence, hitorf_id, BlastHelper.marshallIterationToString(iteration));
    }

    public static BlastHit get(int id_genomes, long id_blasts, long orfs_id, String sequence, long hitorf_id, String textIteration) {
        return new BlastHit(id_genomes, id_blasts, orfs_id, sequence, hitorf_id, textIteration);
    }
}
