package db.mysql;

import analisys.bbh.BidirectionalBlastHit;
import analisys.bbh.BlastHit;
import blast.blast.BlastHelper;
import blast.output.Iteration;
import db.BlastDAO;
import db.GenomeDAO;
import db.OrfDAO;
import format.text.LargeFormat;
import org.apache.commons.dbcp2.BasicDataSource;
import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.Genome;
import sequence.nucleotide.genome.LargeChromosome;
import sequence.nucleotide.genome.LargeGenome;
import sequence.protein.ORF;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
 * Created by alext on 6/11/14.
 * TODO document class
 */
public class GMySQLConnector extends MySQLConnector implements GenomeDAO, OrfDAO, BlastDAO {

    public static final String COMMENT_TEMPLATE = "Comment template";
    protected final BasicDataSource connectionPool;

    /**
     * @param URL      {@link String} of the database
     * @param user     {@link String} user name for the database
     * @param password {@link String} password for the given user
     */
    protected GMySQLConnector(String URL, String user, String password) {
        super(URL, user, password);
        this.connectionPool = new BasicDataSource();
        this.connectionPool.setDriverClassName(MYSQL_DRIVER);
        this.connectionPool.setUrl(URL);
        this.connectionPool.setUsername(user);
        this.connectionPool.setPassword(password);
        this.connectionPool.setPoolPreparedStatements(true);
    }

    public void setNumberOfConnections(int numberOfConnections) {
        this.connectionPool.setInitialSize(numberOfConnections);
        this.connectionPool.setMaxOpenPreparedStatements(numberOfConnections);
    }

    @Override
    public int saveGenomeForName(Genome<? extends Chromosome> genome) throws Exception {
        return 0;
    }

    @Override
    public int saveLargeGenome(LargeGenome genome) throws SQLException {
        int id_genome = 0;
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO `gblaster`.`genomes` ( `name`, `comment`) " +
                        "VALUES ( ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, genome.getName());
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
    public boolean genomeForNameExists(String name) throws SQLException {
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("select id_genomes from `gblaster`.`genomes` where name=?")) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }

        }
        return false;
    }

    @Override
    public int saveChromososmeForGenomeID(int genomeId, Chromosome chromosome) throws SQLException {


        return 0;
    }

    @Override
    public int saveLargeChromososmeForGenomeID(int genomeId, LargeChromosome largeChromosome) throws SQLException, IOException {
        int id_chormosome = 0;
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO `gblaster`.`chromosomes` (`id_genome`, `name`, `sequence`) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
             Reader reader = new InputStreamReader(largeChromosome.getSequenceInputstream())) {

            preparedStatement.setInt(1, genomeId);
            preparedStatement.setString(2, largeChromosome.getAc());
            preparedStatement.setCharacterStream(3, reader);
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
    public Optional<Chromosome> loadCrhomosomeForID(int id) throws SQLException {
        return Optional.empty();
    }

    @Override
    public Optional<LargeChromosome> loadLargeCrhomosomeForID(int id, LargeFormat largeFormat) throws SQLException {

        final PreparedStatement preparedStatement = this.connection.prepareStatement("select * from `gblaster`.`chromosomes` where id_chromosomes=?");
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
    public IntStream loadChromosomeIdsForGenomeId(int genomeId) throws SQLException {
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("select id_chromosomes from `gblaster`.`chromosomes` where id_genome=?")) {
            preparedStatement.setInt(1, genomeId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            final IntStream.Builder builder = IntStream.builder();
            while (resultSet.next()) {
                builder.accept(resultSet.getInt(1));
            }
            return builder.build();
        }
    }

    /**
     * Note that the connection should be set to {@code false} autocommit if the database supports transactions
     *
     * @param genomeId
     * @param chromoStream
     * @return
     * @throws Exception
     */
    @Override
    public IntStream saveLargeChromosomesForGenomeId(int genomeId, Stream<? extends LargeChromosome> chromoStream, int batchSize) throws SQLException {

        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO `gblaster`.`chromosomes` (`id_genome`, `name`, `sequence`) VALUES (?, ?, ?);")) {
            final int[] counter = {0};
            chromoStream.forEach(ch -> {
                final Reader reader = new InputStreamReader(ch.getSequenceInputstream());
                try {
                    preparedStatement.setInt(1, genomeId);
                    preparedStatement.setInt(1, genomeId);
                    preparedStatement.setString(2, ch.getAc());
                    preparedStatement.setCharacterStream(3, reader);
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
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("select id_chromosomes from `gblaster`.`chromosomes` where id_genome=?")) {
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
    public Stream<LargeChromosome> loadLargeChromosomesForGemomeID(int genomeId, LargeFormat largeFormat) throws SQLException {
        PreparedStatement preparedStatement = this.connection
                .prepareStatement("select * from `gblaster`.`chromosomes` where id_genome=?");
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

    @Override //TODO correct keys!!!!!!!!
    public IntStream saveOrfsForChromosomeId(int idChromosome, Stream<? extends ORF> orfStream, int batchSize) throws SQLException {

        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO `gblaster`.`orfs` (`id_chromosome`, `frame`,`start`,`stop`, `name`, `sequence`,`length`) VALUES (?, ?, ?, ?, ?, ?,?);", Statement.RETURN_GENERATED_KEYS)) {
            final int[] countHolder = {0};
            orfStream.forEach(orf -> {
                try {
                    preparedStatement.setInt(1, idChromosome);
                    preparedStatement.setInt(2, orf.getFrame());
                    preparedStatement.setInt(3, orf.getStart());
                    preparedStatement.setInt(4, orf.getStop());
                    preparedStatement.setString(5, orf.getAc());
                    preparedStatement.setString(6, orf.getSequence());
                    preparedStatement.setInt(7, orf.getSequence().length());
                    preparedStatement.addBatch();
                    countHolder[0]++;
                    if (countHolder[0] > batchSize) {
                        countHolder[0] = 0;
                        preparedStatement.executeBatch();
                        this.connection.commit();
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
    public Stream<ORF> loadAllOrfsForGenomeId(int genomeId, int balancer, int minLength, int maxLength) throws SQLException {
        final PreparedStatement statement = this.connection
                .prepareStatement("select orf_sequence, orf_name, start, stop, frame, id_orfs from gblaster.orfs_by_genome_view where id_genomes=?"
                                + " and orf_length>=?"
                                + " and orf_length<=?",
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

    /**
     * @param name
     * @return 0 in case could not find a genome, any other int - in case one could be found
     * @throws Exception
     */
    @Override
    public int genomeIdByName(String name) throws Exception {

        try (PreparedStatement preparedStatement = this.connection.prepareStatement("select id_genomes from `gblaster`.`genomes` where name=?")) {

            preparedStatement.setString(1, name);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }

    @Override
    public int saveBlastResult(Iteration iteration) throws Exception {
        int result = 0;
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("INSERT INTO `gblaster`.`blasts`\n" +
                "(`orfs_id`,\n" +
                "`hitorf_id`,\n" +
                "`report`)\n" +
                "VALUES\n" +
                "(?,\n" +
                "?,\n" +
                "?);\n", Statement.RETURN_GENERATED_KEYS);
        ) {
            final long orfs_id = Long.parseLong(iteration.getIterationQueryDef().split("\\|")[0]);
            final long hitorf_id = Long.parseLong(iteration.getIterationHits().getHit().get(0).getHitDef().split("\\|")[0]);
            preparedStatement.setLong(1, orfs_id);
            preparedStatement.setLong(2, hitorf_id);
            preparedStatement.setString(3, BlastHelper.marshallIterationToString(iteration));
            preparedStatement.executeUpdate();
            this.connection.commit();

            final ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        }
        return result;
    }

    @Override
    public boolean genomeHasBeenBlastedOver(properties.jaxb.Genome query, properties.jaxb.Genome target) throws Exception {
        System.out.println(query.getName().getName() + " -> " + target.getName().getName());
        final int query_genome_id = this.genomeIdByName(query.getName().getName());
        final int target_genome_id = this.genomeIdByName(target.getName().getName());

        try (Statement statement =
                     this.connection.createStatement(
                     )) {

            final ResultSet resultSet = statement.executeQuery(
                    "select A.id_blasts from" +
                            "(SELECT * FROM gblaster.gco_no_sequence_blast_view where id_genomes=" + query_genome_id + ") as A " +
                            ", " +
                            "(SELECT * FROM gblaster.gco_no_sequence_view where id_genomes=" + target_genome_id + ") as B " +
                            "where A.hitorf_id=B.id_orfs " +
                            "limit 1"
            );
            return resultSet.next();
        }
    }

    @Override
    public long calculateOrfsForGenomeName(String genomeName, int minLength, int maxLength) throws Exception {
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("select count(*) from `gblaster`.`gco_view` where genome_name=?"
                                + " and orf_length>=?"
                                + " and orf_length<=?"
                )) {
            preparedStatement.setString(1, genomeName);
            preparedStatement.setInt(2, minLength);
            preparedStatement.setInt(3, maxLength);
            final ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    @Override
    public Stream<BidirectionalBlastHit> getBBHforGenomePair(properties.jaxb.Genome one, properties.jaxb.Genome two, int balancer) throws Exception {
        final int query_genome_id = this.genomeIdByName(one.getName().getName());
        final int target_genome_id = this.genomeIdByName(two.getName().getName());
        try {

            PreparedStatement preparedStatement = this.connection.prepareStatement(
                    "select \n" +
                            "\n" +
                            "A.query_genome_id,\n" +
                            "A.blast_id,\n" +
                            "A.id_orfs,\n" +
                            "A.sequence,\n" +
                            "A.report,\n" +
                            "\n" +
                            "B.query_genome_id,\n" +
                            "B.blast_id,\n" +
                            "B.id_orfs,\n" +
                            "B.sequence,\n" +
                            "B.report\n" +
                            "\n" +
                            "from\n" +
                            "(select query_genome_id,blast_id,id_orfs, hitorf_id,report, sequence from \n" +
                            "(select query_genome_id,blast_id,orfs_id, hitorf_id,report from \n" +
                            "(select query_genome_id,blast_id from gblaster.blastlinks\n" +
                            "where query_genome_id=? and target_genome_id=?) as L\n" +
                            "inner join gblaster.blasts as LB on L.blast_id=LB.id_blasts\n" +
                            ") as LBA\n" +
                            "inner join gblaster.orfs as LO on LBA.orfs_id=LO.id_orfs\n" +
                            ") as A\n" +
                            ",\n" +
                            "(select query_genome_id,blast_id,id_orfs, hitorf_id,report, sequence from\n" +
                            "(select query_genome_id,blast_id,orfs_id, hitorf_id,report from \n" +
                            "(select query_genome_id,blast_id from gblaster.blastlinks\n" +
                            "where query_genome_id=? and target_genome_id=?) as R \n" +
                            "inner join gblaster.blasts as RB on R.blast_id=RB.id_blasts\n" +
                            ") as LBB\n" +
                            "inner join gblaster.orfs as RO on LBB.orfs_id=RO.id_orfs\n" +
                            ")as B\n" +
                            "\n" +
                            "\n" +
                            "where B.id_orfs=A.hitorf_id and A.id_orfs=B.hitorf_id\n;"

                    , balancer, ResultSet.CONCUR_READ_ONLY
            );

            preparedStatement.setFetchSize(balancer);
            preparedStatement.setInt(1, query_genome_id);
            preparedStatement.setInt(2, target_genome_id);
            preparedStatement.setInt(3, target_genome_id);
            preparedStatement.setInt(4, query_genome_id);
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
    public synchronized boolean saveBlastResultBatch(Stream<Iteration> iterations) throws Exception {
        try (
                PreparedStatement preparedStatement = this.connection.prepareStatement(
                        "INSERT INTO `gblaster`.`blasts`\n" +
                                "(`orfs_id`,\n" +
                                "`hitorf_id`,\n" +
                                "`report`)\n" +
                                "VALUES\n" +
                                "(?,\n" +
                                "?,\n" +
                                "?);\n"
                )) {

            iterations.forEach(iter -> {
                try {
                    final long orfs_id = Long.parseLong(iter.getIterationQueryDef().split("\\|")[0]);
                    final long hitorf_id = Long.parseLong(iter.getIterationHits().getHit().get(0).getHitDef().split("\\|")[0]);
                    preparedStatement.setLong(1, orfs_id);
                    preparedStatement.setLong(2, hitorf_id);
                    preparedStatement.setString(3, BlastHelper.marshallIterationToString(iter));
                    preparedStatement.addBatch();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            });
            preparedStatement.executeBatch();
            this.connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return true;
    }

    @Override
    public long reportORFBaseSize(properties.jaxb.Genome genome) throws Exception {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("select count(id_orfs) from gblaster.gco_no_sequence_view where genome_name=?")) {
            preparedStatement.setString(1, genome.getName().getName());
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                return 0;
            }
        }
    }

    public static GMySQLConnector get(String URL, String user, String password) {
        return new GMySQLConnector(URL, user, password);
    }
}

