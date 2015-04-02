package db.mysql;

import analisys.bbh.BidirectionalBlastHit;
import analisys.bbh.BlastHit;
import analisys.bbh.UnidirectionalBlastHit;
import blast.blast.BlastHelper;
import blast.ncbi.output.Iteration;
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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
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
    public Optional<Integer> genomeIDByChromosomeID(int chromosomeID) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeAllChromosomesForGenomeID(int genomeId) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public boolean removeGenomeForName(String name) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public int saveGenomeForName(String genome) throws SQLException {
        int id_genome = 0;
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO `gblaster`.`genomes` ( `name`, `comment`) " +
                        "VALUES ( ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
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
    public int saveGenomeForName(Genome<? extends Chromosome> genome) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public int saveLargeGenome(LargeGenome genome) throws SQLException {

        return saveGenomeForName(genome.getName());
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
        throw new NotImplementedException();
    }

    @Override
    public int saveMockChromosome(int genomeId) throws Exception {
        int id_chormosome = 0;
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO `gblaster`.`chromosomes` (`id_genome`, `name`, `sequence`) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
             ) {

            preparedStatement.setInt(1, genomeId);
            preparedStatement.setString(2, "MockAC_genome: "+genomeId);
            preparedStatement.setString(3, "ATGC");
            preparedStatement.executeUpdate();
            this.connection.commit();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                id_chormosome = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            this.connection.rollback();
            throw e;
        }
        return id_chormosome;
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
                .prepareStatement("INSERT INTO `gblaster`.`orfs` " +
                        "(`id_chromosome`, `frame`,`start`,`stop`, `name`, `sequence`,`length`) " +
                        "VALUES (?, ?, ?, ?, ?, ?,?);", Statement.RETURN_GENERATED_KEYS)) {
            final int[] countHolder = {0};
            orfStream.forEach(orf -> {
                try {
                    int position=0;
                    preparedStatement.setInt(++position, idChromosome);
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
    public int saveBlastResult(Iteration iteration, int qgenome_id, int tgenome_id) throws Exception {
        int result = 0;
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("" +
                "INSERT INTO `gblaster`.`gblasts`\n" +
                "(`orfs_id`,\n" +
                "`hitorf_id`,\n" +
                "`report`," +
                "`qgenome_id`," +
                "`tgenome_id`)\n" +
                "VALUES\n" +
                "(?,\n" +
                "?,\n" +
                "?," +
                "?," +
                "?);\n", Statement.RETURN_GENERATED_KEYS);
        ) {
            final long orfs_id = Long.parseLong(iteration.getIterationQueryDef().split("\\|")[0]);
            final long hitorf_id = Long.parseLong(iteration.getIterationHits().getHit().get(0).getHitDef().split("\\|")[0]);
            preparedStatement.setLong(1, orfs_id);
            preparedStatement.setLong(2, hitorf_id);
            preparedStatement.setString(3, BlastHelper.marshallIterationToString(iteration));
            preparedStatement.setInt(4, qgenome_id);
            preparedStatement.setInt(5, tgenome_id);
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
    public int saveBitsScore(Iteration iteration, long id_blasts) throws Exception {
        int result = 0;
        final double bitscore = BlastHelper.comulativeBitScore(iteration);
        try (PreparedStatement preparedStatement = this.connection.prepareStatement("INSERT INTO " +
                "`gblaster`.`bitscores` " +
                "(`blast_id`, `bitscore`) VALUES (?, ?);\n", Statement.RETURN_GENERATED_KEYS);
        ) {
            final long orfs_id = Long.parseLong(iteration.getIterationQueryDef().split("\\|")[0]);
            final long hitorf_id = Long.parseLong(iteration.getIterationHits().getHit().get(0).getHitDef().split("\\|")[0]);
            preparedStatement.setLong(1, id_blasts);
            preparedStatement.setDouble(2, bitscore);
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
                    "select \n" +
                            "id_blasts\n" +
                            "from \n" +
                            "gblaster.gblasts\n" +
                            "where \n" +
                            "qgenome_id=" + query_genome_id + "\n" +
                            "and\n" +
                            "tgenome_id=" + target_genome_id + "\n" +
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
                    "select\n" +
                            "BL.qgenome_id,\n" +
                            "BL.id_blasts,\n" +
                            "LO.id_orfs,\n" +
                            "LO.sequence,\n" +
                            "BL.report,\n" +
                            "BR.qgenome_id,\n" +
                            "BR.id_blasts,\n" +
                            "RO.id_orfs,\n" +
                            "RO.sequence,\n" +
                            "BR.report\n" +
                            "from \n" +
                            "gblaster.gblasts BL\n" +
                            "inner join\n" +
                            "gblaster.gblasts BR\n" +
                            "on BL.qgenome_id=BR.tgenome_id\n" +
                            "and BR.qgenome_id=BL.tgenome_id\n" +
                            "and BL.orfs_id=BR.hitorf_id\n" +
                            "and BR.orfs_id=BL.hitorf_id\n" +
                            "inner join \n" +
                            "gblaster.orfs LO\n" +
                            "on BL.orfs_id=LO.id_orfs\n" +
                            "inner join \n" +
                            "gblaster.orfs RO\n" +
                            "on BR.orfs_id=RO.id_orfs\n" +
                            "where \n" +
                            "BL.qgenome_id=?\n" +
                            "and \n" +
                            "BR.qgenome_id=?;"
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
    public Stream<UnidirectionalBlastHit> getBHforGenomePair(properties.jaxb.Genome one, properties.jaxb.Genome two, double cutoff, int balancer) throws Exception {
        final int query_genome_id = this.genomeIdByName(one.getName().getName());
        final int target_genome_id = this.genomeIdByName(two.getName().getName());
        try {

            PreparedStatement preparedStatement = this.connection.prepareStatement(
                    "SELECT \n" +
                            "\n" +
                            "LO.id_orfs,\n" +
                            "LO.sequence,\n" +
                            "RO.id_orfs,\n" +
                            "RO.sequence,\n" +
                            "B.id_blasts,\n" +
                            "B.report\n" +
                            //"BS.bitscore\n" +
                            //" \n" +
                            "from\n" +
                            "gblaster.gblasts B\n" +
                            "inner join \n" +
                            "gblaster.orfs LO\n" +
                            "on B.orfs_id=LO.id_orfs\n" +
                            "inner join\n" +
                            "gblaster.orfs RO\n" +
                            "on B.hitorf_id=RO.id_orfs\n" +
                            //"inner join \n" +
                            //"gblaster.bitscores BS\n" +
                            //"on B.id_blasts=BS.blast_id\n" +
                            "where \n" +
                            "B.qgenome_id=?\n" +
                            "and \n" +
                            "B.tgenome_id=?\n" +
                            //"and\n" +
                            //"BS.bitscore >=?"
                            ";"
                    , ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY
            );

            preparedStatement.setFetchSize(balancer);
            preparedStatement.setInt(1, query_genome_id);
            preparedStatement.setInt(2, target_genome_id);
            //preparedStatement.setDouble(3, cutoff);
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
                                resultSet.getString(6), cutoff, resultSet.getString(4));

                        return unidirectionalBlastHit;
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
    public synchronized boolean saveBlastResultBatch(Stream<Iteration> iterations, int qgenome_id, int tgenome_id) throws Exception {
        try (
                PreparedStatement preparedStatement = this.connection.prepareStatement(
                        "INSERT INTO `gblaster`.`gblasts`\n" +
                                "(`orfs_id`,\n" +
                                "`hitorf_id`,\n" +
                                "`report`,\n" +
                                "`qgenome_id`,\n" +
                                "`tgenome_id`)\n" +
                                "VALUES" +
                                "(?," +
                                "?," +
                                "?," +
                                "?," +
                                "?);"

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

