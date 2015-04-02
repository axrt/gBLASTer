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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    public Optional<Integer> genomeIDByChromosomeID(int chromosomeID) throws Exception {

        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("select id_genome from app.chromosomes where id_chromosome=?")){
            preparedStatement.setInt(1,chromosomeID);
            final ResultSet resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                return Optional.of(resultSet.getInt(1));
            }else{
                throw new Exception("Could not find a corresponding genome for chromosome_id: "+chromosomeID);
            }
        }

    }

    @Override
    public boolean removeAllChromosomesForGenomeID(int genomeId) throws Exception {
       throw new NotImplementedException();
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
        throw new NotImplementedException();
    }

    @Override
    public int saveBitsScore(Iteration iteration, long id_blasts) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public boolean genomeHasBeenBlastedOver(Genome query, Genome target) throws Exception {


        throw new NotImplementedException();
    }

    @Override
    public Stream<BidirectionalBlastHit> getBBHforGenomePair(Genome one, Genome two, int balancer) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public boolean saveBlastResultBatch(Stream<Iteration> iterations, int qgenome_id, int tgenome_id) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public Stream<UnidirectionalBlastHit> getBHforGenomePair(Genome one, Genome two, double cutoff, int balancer) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public int saveGenomeForName(sequence.nucleotide.genome.Genome<? extends Chromosome> genome) throws Exception {
        throw new NotImplementedException();
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
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("select id_genome from app.genomes where name=?")) {

            preparedStatement.setString(1, name);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }else{
             throw new Exception("Genome by name \""+name+"\" does not exist in the database!");
            }
        }
    }

    @Override
    public int saveMockChromosome(int genomeId) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public int saveChromososmeForGenomeID(int genomeId, Chromosome chromosome) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public int saveLargeChromososmeForGenomeID(int genomeId, LargeChromosome largeChromosome) throws Exception {
        int id_chormosome = 0;
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO app.chromosomes (id_genome, name, sequence) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             InputStream inputStream=largeChromosome.getSequenceInputstream()) {

            preparedStatement.setInt(1, genomeId);
            preparedStatement.setString(2, largeChromosome.getAc());
            preparedStatement.setBinaryStream(3, inputStream);
            preparedStatement.executeUpdate();
            this.connection.commit();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                id_chormosome = resultSet.getInt(1);
            }
        } catch (SQLException | IOException e) {
            this.connection.rollback();
            throw e;
        }
        return id_chormosome;
    }

    @Override
    public Optional<Chromosome> loadCrhomosomeForID(int id) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public Optional<LargeChromosome> loadLargeCrhomosomeForID(int id, LargeFormat largeFormat) throws Exception {

        final PreparedStatement preparedStatement = this.connection.prepareStatement("select * from app.chromosomes where id_chromosome=?");
        try {
            preparedStatement.setInt(1, id);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(LargeChromosome.formPreprocessedComponents(resultSet.getString(3), resultSet.getBinaryStream(4), largeFormat));
            } else {
                return Optional.empty();
            }
        } catch (RuntimeException e) {
            preparedStatement.close();
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Override
    public IntStream loadChromosomeIdsForGenomeId(int genomeId) throws Exception {
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("select id_chromosome from app.chromosomes where id_genome=?")) {
            preparedStatement.setInt(1, genomeId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            final IntStream.Builder builder = IntStream.builder();
            while (resultSet.next()) {
                builder.accept(resultSet.getInt(1));
            }
            return builder.build();
        }
    }

    @Override
    public IntStream saveLargeChromosomesForGenomeId(int genomeId, Stream<? extends LargeChromosome> chromoStream, int batchSize) throws Exception {
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO app.chromosomes (id_genome, name, sequence) VALUES (?, ?, ?)")) {
            final int[] counter = {0};
            chromoStream.forEach(ch -> {
                final InputStream inputStream = ch.getSequenceInputstream();
                try {
                    preparedStatement.setInt(1, genomeId);
                    preparedStatement.setString(2, ch.getAc());
                    preparedStatement.setBinaryStream(3, inputStream);
                    preparedStatement.addBatch();
                    counter[0]++;
                    if (counter[0] > batchSize) {
                        counter[0] = 0;
                        preparedStatement.executeBatch();
                        this.connection.commit();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            preparedStatement.executeBatch();
            this.connection.commit();
        }
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("select id_chromosome from app.chromosomes where id_genome=?")) {
            preparedStatement.setInt(1, genomeId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            final IntStream.Builder builder = IntStream.builder();
            while (resultSet.next()) {
                builder.accept(resultSet.getInt(1));
            }
            return builder.build();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Override
    public Stream<LargeChromosome> loadLargeChromosomesForGemomeID(int genomeId, LargeFormat largeFormat) throws Exception {

        PreparedStatement preparedStatement = this.connection
                .prepareStatement("select * from app.chromosomes where id_genome=?");
        try {
            preparedStatement.setInt(1, genomeId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            final Iterator<LargeChromosome> iter = new Iterator<LargeChromosome>() {
                @Override
                public boolean hasNext() {
                    try {
                        if (resultSet.next()) {
                            return true;
                        } else {
                            preparedStatement.close();
                            return false;
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public LargeChromosome next() {
                    try {

                        return LargeChromosome.formPreprocessedComponents(resultSet.getString(3), resultSet.getBinaryStream(4), largeFormat);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iter, Spliterator.NONNULL), false);
        } catch (RuntimeException e) {
            preparedStatement.close();
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Override
    public IntStream saveOrfsForChromosomeId(int idChromosome, Stream<? extends ORF> orfStream, int batchSize) throws Exception {

        final int genome_id=this.genomeIDByChromosomeID(idChromosome).get();
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO app.orfs " +
                        "(id_chromosome, id_genome, frame, start, stop, name, sequence, length) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?,?)", Statement.RETURN_GENERATED_KEYS)) {
            final int[] countHolder = {0};
            orfStream.forEach(orf -> {
                try {

                    int position=0;
                    preparedStatement.setInt(++position, idChromosome);
                    preparedStatement.setInt(++position, genome_id);
                    preparedStatement.setInt(++position, orf.getFrame());
                    preparedStatement.setInt(++position, orf.getStart());
                    preparedStatement.setInt(++position, orf.getStop());
                    preparedStatement.setString(++position, orf.getAc());
                    preparedStatement.setString(++position, orf.getSequence());
                    preparedStatement.setInt(++position, orf.getSequence().length());
                    preparedStatement.addBatch();
                    countHolder[0]++;

                    if (countHolder[0] > batchSize) {
                        countHolder[0] = 0;
                        preparedStatement.executeBatch();
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            preparedStatement.executeBatch();
            this.connection.commit();
            final ResultSet resultSet = preparedStatement.getGeneratedKeys();
            final IntStream.Builder builder = IntStream.builder();
            while (resultSet.next()) {
                builder.accept(resultSet.getInt(1));
            }
            return builder.build();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Override
    public Stream<ORF> loadAllOrfsForGenomeId(int genomeId, int balancer, int minLength, int maxLength) throws Exception {
        final PreparedStatement statement = this.connection
                .prepareStatement("select sequence, name, start, stop, frame, id_orf from app.orfs where id_genome=?"
                                + " and length>=?"
                                + " and length<=?",
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY
                );
        statement.setFetchSize(balancer);

        try {
            statement.setInt(1, genomeId);
            statement.setInt(2, minLength);
            statement.setInt(3, maxLength);
            final ResultSet resultSet = statement.executeQuery();
            System.out.println("next requested");
            final Iterator<ORF> iter = new Iterator<ORF>() {
                @Override
                public boolean hasNext() {
                    try {
                        if (resultSet.next()) {

                            return true;
                        } else {
                            statement.close();
                            return false;
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public ORF next() {
                    try {
                        final ORF orf = ORF.get(resultSet.getString(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getInt(4), resultSet.getInt(5));
                        orf.setId(resultSet.getLong(6));
                        return orf;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iter, Spliterator.NONNULL), false);
        } catch (RuntimeException e) {
            statement.close();
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Override
    public long calculateOrfsForGenomeName(String genomeName, int minLength, int maxLength) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public long reportORFBaseSize(Genome genome) throws Exception {
        final int gemome_id=this.genomeIdByName(genome.getName().getName());
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("select count(id_orf) from app.orfs where id_genome=?")) {
            preparedStatement.setInt(1, gemome_id);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return 0;
            }
        }
    }
}
