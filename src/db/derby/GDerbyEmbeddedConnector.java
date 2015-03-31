package db.derby;

import analisys.bbh.BidirectionalBlastHit;
import analisys.bbh.UnidirectionalBlastHit;
import blast.ncbi.output.Iteration;
import db.BlastDAO;
import db.GenomeDAO;
import db.OrfDAO;
import format.text.LargeFormat;
import properties.jaxb.Genome;
import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.LargeChromosome;
import sequence.nucleotide.genome.LargeGenome;
import sequence.protein.ORF;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by alext on 3/31/15.
 */
public class GDerbyEmbeddedConnector extends DerbyEmbeddedConnector implements GenomeDAO, OrfDAO, BlastDAO {

    public static final String COMMENT_TEMPLATE = "Comment template";

    /**
     * @param URL      {@link String} of the database
     * @param user     {@link String} user name
     * @param password {@link String} password
     */
    protected GDerbyEmbeddedConnector(String URL, String user, String password) {
        super(URL, user, password);
    }

    public static GDerbyEmbeddedConnector get(String URL, String user, String password){
        return new GDerbyEmbeddedConnector(URL,user,password);
    }

    @Override
    public boolean removeGenomeForName(String name) throws Exception {
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("delete from app.genomes where name=?")){
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
            this.connection.commit();
        }
        return true;
    }

    @Override
    public int saveBlastResult(Iteration iteration, int qgenome_id, int tgenome_id) throws Exception {
        return 0;
    }

    @Override
    public int saveBitsScore(Iteration iteration, long id_blasts) throws Exception {
        return 0;
    }

    @Override
    public boolean genomeHasBeenBlastedOver(Genome query, Genome target) throws Exception {
        return false;
    }

    @Override
    public Stream<BidirectionalBlastHit> getBBHforGenomePair(Genome one, Genome two, int balancer) throws Exception {
        return null;
    }

    @Override
    public boolean saveBlastResultBatch(Stream<Iteration> iterations, int qgenome_id, int tgenome_id) throws Exception {
        return false;
    }

    @Override
    public Stream<UnidirectionalBlastHit> getBHforGenomePair(Genome one, Genome two, double cutoff, int balancer) throws Exception {
        return null;
    }

    @Override
    public int saveGenomeForName(sequence.nucleotide.genome.Genome<? extends Chromosome> genome) throws Exception {
        return 0;
    }

    @Override
    public int saveGenomeForName(String genome) throws Exception {
        int id_genome = 0;
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO app.genomes (name, comment) " +
                        "VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, genome);
            preparedStatement.setString(2, COMMENT_TEMPLATE);
            preparedStatement.executeUpdate();
            this.connection.commit();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                id_genome = resultSet.getInt(1);
            }
        }
        return id_genome;
    }

    @Override
    public int saveLargeGenome(LargeGenome genome) throws Exception {
        return this.saveGenomeForName(genome.getName());
    }

    @Override
    public boolean genomeForNameExists(String name) throws Exception {
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("select id_genome from app.genomes where name=?")) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }

        }
        return false;
    }

    @Override
    public int genomeIdByName(String name) throws Exception {
        return 0;
    }

    @Override
    public int saveMockChromosome(int genomeId) throws Exception {
        return 0;
    }

    @Override
    public int saveChromososmeForGenomeID(int genomeId, Chromosome chromosome) throws Exception {
        return 0;
    }

    @Override
    public int saveLargeChromososmeForGenomeID(int genomeId, LargeChromosome largeChromosome) throws Exception {
        return 0;
    }

    @Override
    public Optional<Chromosome> loadCrhomosomeForID(int id) throws Exception {
        return null;
    }

    @Override
    public Optional<LargeChromosome> loadLargeCrhomosomeForID(int id, LargeFormat largeFormat) throws Exception {
        return null;
    }

    @Override
    public IntStream loadChromosomeIdsForGenomeId(int genomeId) throws Exception {
        return null;
    }

    @Override
    public IntStream saveLargeChromosomesForGenomeId(int genomeId, Stream<? extends LargeChromosome> chromoStream, int counter) throws Exception {
        return null;
    }

    @Override
    public Stream<LargeChromosome> loadLargeChromosomesForGemomeID(int genomeId, LargeFormat largeFormat) throws Exception {
        return null;
    }

    @Override
    public IntStream saveOrfsForChromosomeId(int idChromosome, Stream<? extends ORF> orfStream, int batchSize) throws Exception {
        return null;
    }

    @Override
    public Stream<ORF> loadAllOrfsForGenomeId(int genomeId, int balancer, int minLength, int maxLength) throws Exception {
        return null;
    }

    @Override
    public long calculateOrfsForGenomeName(String genomeName, int minLength, int maxLength) throws Exception {
        return 0;
    }

    @Override
    public long reportORFBaseSize(Genome genome) throws Exception {
        return 0;
    }
}
