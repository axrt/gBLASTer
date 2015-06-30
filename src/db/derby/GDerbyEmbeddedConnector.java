package db.derby;

import analisys.bbh.BidirectionalBlastHit;
import analisys.bbh.BlastHit;
import analisys.bbh.UnidirectionalBlastHit;
import blast.blast.BlastHelper;
import blast.ncbi.output.Iteration;
import db.BlastDAO;
import db.GenomeDAO;
import db.OrfDAO;
import format.text.LargeFormat;
import org.apache.commons.io.input.ReaderInputStream;
import properties.jaxb.Genome;
import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.LargeChromosome;
import sequence.nucleotide.genome.LargeGenome;
import sequence.protein.ORF;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
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

    public static GDerbyEmbeddedConnector get(String URL, String user, String password) {
        return new GDerbyEmbeddedConnector(URL, user, password);
    }

    @Override
    public Optional<Integer> genomeIDByChromosomeID(int chromosomeID) throws Exception {
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("select id_genome from app.chromosomes where id_chromosome=?")) {
            preparedStatement.setInt(1, chromosomeID);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                this.connection.commit();
                return Optional.of(resultSet.getInt(1));
            } else {
                throw new Exception("Could not find a corresponding genome for chromosome_id: " + chromosomeID);
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
                .prepareStatement("delete from app.genomes where name=?")) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
            this.connection.commit();
        } catch (Exception e) {
            this.connection.rollback();
            throw e;
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

        final int query_genome_id = this.genomeIdByName(query.getName().getName());
        final int target_genome_id = this.genomeIdByName(target.getName().getName());

        try (Statement statement =
                     this.connection.createStatement(
                     )) {

            final ResultSet resultSet = statement.executeQuery(
                    "select \n" +
                            "id_progress\n" +
                            "from \n" +
                            "app.progress\n" +
                            "where \n" +
                            "ID_QUERY_GENOME=" + query_genome_id + "\n" +
                            "and\n" +
                            "ID_TARGET_GENOME=" + target_genome_id + "\n" +
                            "FETCH FIRST ROW ONLY"
            );
            this.connection.commit();
            return resultSet.next();
        } catch (Exception e) {
            throw e;
        }
    }



    @Override
    public Stream<BidirectionalBlastHit> getBBHforGenomePair(Genome one, Genome two, int balancer) throws Exception {
        final int query_genome_id = this.genomeIdByName(one.getName().getName());
        final int target_genome_id = this.genomeIdByName(two.getName().getName());
        try {

            PreparedStatement preparedStatement = this.connection.prepareStatement(
                    "select \n" +
                            "BL.ID_QUERY_GENOME,       \n" +
                            "BL.ID_BLAST,\n" +
                            "LO.ID_ORF,\n" +
                            "LO.SEQUENCE,\n" +
                            "BL.ITERATION,\n" +
                            "BR.ID_QUERY_GENOME,\n" +
                            "BR.ID_BLAST,\n" +
                            "RO.ID_ORF,\n" +
                            "RO.SEQUENCE,\n" +
                            "BR.ITERATION\n" +
                            "from\n" +
                            "APP.BLASTS BL\n" +
                            "inner join\n" +
                            "APP.BLASTS BR\n" +
                            "on BL.ID_QUERY_GENOME=BR.ID_TARGET_GENOME\n" +
                            "and BR.ID_QUERY_GENOME=BL.ID_TARGET_GENOME\n" +
                            "and BL.ID_QUERY_ORF=BR.ID_TARGET_ORF\n" +
                            "and BR.ID_QUERY_ORF=BL.ID_TARGET_ORF\n" +
                            "inner join\n" +
                            "APP.ORFS LO\n" +
                            "on BL.ID_QUERY_ORF=LO.ID_ORF\n" +
                            "inner join\n" +
                            "APP.ORFS RO\n" +
                            "on BR.ID_QUERY_ORF=RO.ID_ORF\n" +
                            "where\n" +
                            "BL.ID_QUERY_GENOME=?\n" +
                            "and\n" +
                            "BR.ID_QUERY_GENOME=?"
                    , ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY
            );

            preparedStatement.setFetchSize(balancer);
            preparedStatement.setInt(1, query_genome_id);
            preparedStatement.setInt(2, target_genome_id);
            final ResultSet resultSet = preparedStatement.executeQuery();

            Iterator<BidirectionalBlastHit> iter = new Iterator<BidirectionalBlastHit>() {
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
                public BidirectionalBlastHit next() {
                    try {

                        final BlastHit blastHitOne;

                        blastHitOne = BlastHit.get(
                                resultSet.getInt(1), resultSet.getLong(2), resultSet.getLong(3), resultSet.getString(4), resultSet.getLong(8), resultSet.getString(5));

                        final BlastHit blastHitTwo;

                        blastHitTwo = BlastHit.get(
                                resultSet.getInt(6), resultSet.getLong(7), resultSet.getLong(8), resultSet.getString(9), resultSet.getLong(3), resultSet.getString(10));

                        return new BidirectionalBlastHit(blastHitOne, blastHitTwo);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iter, Spliterator.NONNULL), false);
        } catch (RuntimeException e) {
            throw (Exception) e.getCause();
        }
    }

    @Override
    public boolean saveBlastResultBatch(Stream<Iteration> iterations, int qgenome_id, int tgenome_id) throws Exception {

        try (
                PreparedStatement preparedStatement = this.connection.prepareStatement(
                        "INSERT INTO APP.BLASTS \n" +
                                "(ID_QUERY_ORF,\n" +
                                "ID_TARGET_ORF,\n" +
                                "ITERATION,\n" +
                                "ID_QUERY_GENOME,\n" +
                                "ID_TARGET_GENOME,\n" +
                                "SCORE)\n" +
                                "VALUES" +
                                "(?," +
                                "?," +
                                "?," +
                                "?," +
                                "?," +
                                "?)"
                );
        ) {

            iterations.forEach(iter -> {
                try {
                    final int orfs_id = Integer.parseInt(iter.getIterationQueryDef().split("\\|")[0]);
                    final int hitorf_id = Integer.parseInt(iter.getIterationHits().getHit().get(0).getHitDef().split("\\|")[0]);
                    preparedStatement.setInt(1, orfs_id);
                    preparedStatement.setInt(2, hitorf_id);
                    preparedStatement.setString(3, BlastHelper.marshallIterationToString(iter));
                    preparedStatement.setInt(4, qgenome_id);
                    preparedStatement.setInt(5, tgenome_id);
                    preparedStatement.setDouble(6, BlastHelper.comulativeBitScore(iter));
                    preparedStatement.addBatch();

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            });
            preparedStatement.executeBatch();
            this.connection.commit();
        } catch (Exception e) {
            this.connection.rollback();
            throw e;
        }
        return true;
    }

    @Override
    public Stream<UnidirectionalBlastHit> getBHforGenomePair(Genome one, Genome two, double cutoff, int balancer) throws Exception {
        final int query_genome_id = this.genomeIdByName(one.getName().getName());
        final int target_genome_id = this.genomeIdByName(two.getName().getName());
        try {

            PreparedStatement preparedStatement = this.connection.prepareStatement(
                    "SELECT\n" +
                            "LO.ID_ORF,\n" +
                            "LO.SEQUENCE,\n" +
                            "RO.ID_ORF,\n" +
                            "RO.SEQUENCE,\n" +
                            "B.ID_BLAST,\n" +
                            "B.ITERATION,\n" +
                            "B.SCORE\n" +
                            "from\n" +
                            "APP.BLASTS B\n" +
                            "inner join\n" +
                            "APP.ORFS LO\n" +
                            "on B.ID_QUERY_ORF=LO.ID_ORF\n" +
                            "inner join\n" +
                            "APP.ORFS RO\n" +
                            "on B.ID_TARGET_ORF=RO.ID_ORF\n" +
                            "where\n" +
                            "B.ID_QUERY_GENOME=?\n" +
                            "and\n" +
                            "B.ID_TARGET_GENOME=?\n" +
                            "and\n" +
                            "B.SCORE >=?"
                    , ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY
            );

            preparedStatement.setFetchSize(balancer);
            preparedStatement.setInt(1, query_genome_id);
            preparedStatement.setInt(2, target_genome_id);
            preparedStatement.setDouble(3, cutoff);
            final ResultSet resultSet = preparedStatement.executeQuery();

            Iterator<UnidirectionalBlastHit> iter = new Iterator<UnidirectionalBlastHit>() {
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
                public UnidirectionalBlastHit next() {
                    try {

                        final UnidirectionalBlastHit unidirectionalBlastHit = UnidirectionalBlastHit.get(
                                query_genome_id, target_genome_id, resultSet.getInt(5),
                                resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3),
                                resultSet.getString(6), resultSet.getDouble(7), resultSet.getString(4));

                        return unidirectionalBlastHit;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    iter, Spliterator.NONNULL), false);
        } catch (RuntimeException e) {
            throw (Exception) e.getCause();
        }
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
            final ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                id_genome = resultSet.getInt(1);
            }
            this.connection.commit();
        } catch (Exception e) {
            this.connection.rollback();
            throw e;
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
        this.connection.commit();
        return false;
    }

    @Override
    public int genomeIdByName(String name) throws Exception {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("select id_genome from app.genomes where name=?")) {
            preparedStatement.setString(1, name);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                this.connection.commit();
                return resultSet.getInt(1);
            } else {
                throw new Exception("Genome by name \"" + name + "\" does not exist in the database!");
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
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO app.chromosomes (id_genome, name, sequence) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             InputStreamReader inputStreamReader = new InputStreamReader(largeChromosome.getSequenceInputstream())) {

            preparedStatement.setInt(1, genomeId);
            preparedStatement.setString(2, largeChromosome.getAc());
            preparedStatement.setClob(3, inputStreamReader);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                throw new Exception("Save operation failed to return chromosome ID!");
            }
        } catch (SQLException | IOException e) {
            this.connection.rollback();
            throw e;
        }
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
                return Optional.of(LargeChromosome.formPreprocessedComponents(resultSet.getString(3),
                        new ReaderInputStream(resultSet.getCharacterStream(4)), largeFormat));
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
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public IntStream saveLargeChromosomesForGenomeId(int genomeId, Stream<? extends LargeChromosome> chromoStream, int batchSize) throws Exception {

        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO app.chromosomes (id_genome, name, sequence) VALUES (?, ?, ?)")) {
            final int[] counter = {0};
            chromoStream.forEach(ch -> {
                final Reader reader = new InputStreamReader(ch.getSequenceInputstream());
                try {
                    preparedStatement.setInt(1, genomeId);
                    preparedStatement.setString(2, ch.getAc());
                    preparedStatement.setClob(3, reader);
                    System.out.println("Added chromosome: " + ch.getAc());
                    preparedStatement.addBatch();
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    //counter[0]++;
                    //if (counter[0] > batchSize) {
                    //counter[0] = 0;
                    //preparedStatement.executeBatch();
                    // }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            preparedStatement.executeBatch();
            this.connection.commit();
        } catch (Exception e) {
            this.connection.rollback();
            throw e;
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
            this.connection.rollback();
            if (e.getCause() instanceof SQLException) {
                throw (SQLException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Override
    public Stream<LargeChromosome> loadLargeChromosomesForGenomeID(int genomeId, LargeFormat largeFormat) throws Exception {

        PreparedStatement preparedStatement = this.connection
                .prepareStatement("select * from app.chromosomes where id_genome=?", ResultSet.TYPE_FORWARD_ONLY);
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

                        return LargeChromosome.formPreprocessedComponents(resultSet.getString(3),
                                new ReaderInputStream(resultSet.getCharacterStream(4)), largeFormat);
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

        final int genome_id = this.genomeIDByChromosomeID(idChromosome).get();
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO app.orfs " +
                        "(id_chromosome, id_genome, frame, start, stop, name, sequence, length) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?,?)", Statement.RETURN_GENERATED_KEYS)) {
            final int[] countHolder = {0};
            orfStream.forEach(orf -> {
                try {

                    int position = 0;
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
            if (resultSet != null) {
                final IntStream.Builder builder = IntStream.builder();

                while (resultSet.next()) {
                    builder.accept(resultSet.getInt(1));
                }
                return builder.build();
            } else {
                return IntStream.empty();
            }
        } catch (RuntimeException e) {
            this.connection.rollback();
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
            System.out.println("Next set of ORFS requested...");
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
            this.connection.commit();
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

        final int genome_id = this.genomeIdByName(genomeName);

        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("select count(*) from app.orfs where id_genome=?"
                                + " and length>=?"
                                + " and length<=?"
                )) {
            preparedStatement.setInt(1, genome_id);
            preparedStatement.setInt(2, minLength);
            preparedStatement.setInt(3, maxLength);
            final ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            this.connection.commit();
            return resultSet.getInt(1);
        }
    }

    @Override
    public long reportORFBaseSize(Genome genome) throws Exception {
        final int gemome_id = this.genomeIdByName(genome.getName().getName());
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("select count(id_orf) from app.orfs where id_genome=?")) {
            preparedStatement.setInt(1, gemome_id);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                this.connection.commit();
                return resultSet.getLong(1);
            } else {
                this.connection.commit();
                return 0;
            }
        }
    }

    @Override
    public int setBlastedPair(Genome queryGenome, Genome targetGenome) throws Exception {

        final int queryGenomeID = this.genomeIdByName(queryGenome.getName().getName());
        final int targetGenomeID = this.genomeIdByName(targetGenome.getName().getName());

        int returnKey=0;
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO app.progress " +
                        "(id_query_genome, id_target_genome) " +
                        "VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1,queryGenomeID);
            preparedStatement.setInt(2,targetGenomeID);
            preparedStatement.executeUpdate();
            this.connection.commit();
            final ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if(resultSet.next()){
                returnKey=resultSet.getInt(1);
            }
        }

        return returnKey;
    }
}
