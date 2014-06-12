package db.mysql;

import db.GenomeDAO;
import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.Genome;
import sequence.nucleotide.genome.LargeChromosome;

import java.io.IOException;

/**
 * Created by alext on 6/11/14.
 * TODO document class
 */
public class GMySQLConnector extends MySQLConnector implements GenomeDAO {
    /**
     * @param URL      {@link String} of the database
     * @param user     {@link String} user name for the database
     * @param password {@link String} password for the given user
     */
    protected GMySQLConnector(String URL, String user, String password) {
        super(URL, user, password);
    }

    @Override
    public int saveGenomeForName(Genome<? extends Chromosome> genome) throws Exception {
        return 0;
    }

    @Override
    public boolean genomeForNameExists(String name) throws Exception {
        return false;
    }

    @Override
    public int saveChromososmeForGenomeID(int genomeId, Chromosome chromosome) throws IOException {
        return 0;
    }

    @Override
    public int saveLargeChromososmesForGenomeID(int genomeId, LargeChromosome largeChromosome) throws IOException {
        return 0;
    }

    @Override
    public Chromosome loadCrhomosomeForID(int id) throws IOException {
        return null;
    }

    @Override
    public LargeChromosome loadLargeCrhomosomeForID(int id) throws IOException {
        return null;
    }
}
