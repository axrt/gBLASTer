package db.mysql;

import blast.blast.BlastHelper;
import blast.output.Iteration;
import db.BlastDAO;
import db.GenomeDAO;
import db.OrfDAO;
import format.text.LargeFormat;
import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.Genome;
import sequence.nucleotide.genome.LargeChromosome;
import sequence.nucleotide.genome.LargeGenome;
import sequence.protein.ORF;

import java.io.*;
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
    public int saveLargeGenome(LargeGenome genome) throws SQLException {
        int id_genome = 0;
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO `gblaster`.`genomes` ( `name`, `comment`) " +
                        "VALUES ( ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, genome.getName());
            preparedStatement.setString(2, COMMENT_TEMPLATE);
            preparedStatement.executeUpdate();
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
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                id_chormosome = resultSet.getInt(1);
            }
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
                .prepareStatement("INSERT INTO `gblaster`.`chromosomes` (`id_genome`, `name`, `sequence`) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS)) {
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
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            preparedStatement.executeBatch();
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

    @Override
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
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            preparedStatement.executeBatch();
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
    public Stream<ORF> loadAllOrfsForGenomeId(int genomeId, int balancer,int minLength, int maxLength) throws SQLException {
        PreparedStatement preparedStatement = this.connection
                .prepareStatement("select * from `gblaster`.`gco_view` where id_genomes=? and orf_length>="
                        +minLength
                        +" and orf_length<="+maxLength
                        ,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        preparedStatement.setFetchSize(balancer);
        try {
            preparedStatement.setInt(1, genomeId);
            final ResultSet resultSet = preparedStatement.executeQuery();
            final Iterator<ORF> iter = new Iterator<ORF>() {
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
                public ORF next() {
                    try {
                        final ORF orf = ORF.get(resultSet.getString(12), resultSet.getString(11), resultSet.getInt(10), resultSet.getInt(9), resultSet.getInt(8));
                        orf.setId(resultSet.getInt(7));
                        return orf;
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
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            final int orfs_id = Integer.valueOf(iteration.getIterationQueryDef().split("\\|")[0]);
            final int hitorf_id = Integer.valueOf(iteration.getIterationHits().getHit().get(0).getHitDef().split("\\|")[0]);
            preparedStatement.setInt(1, orfs_id);
            preparedStatement.setInt(2, hitorf_id);
            BlastHelper.marshalBlast(iteration, byteArrayOutputStream);
            try (InputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                 Reader reader = new BufferedReader(new InputStreamReader(byteArrayInputStream))) {
                preparedStatement.setCharacterStream(3, reader);
                preparedStatement.executeUpdate();
            }
            final ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        }
        return result;
    }

    @Override
    public boolean genomeHasBeenBlastedOver(properties.jaxb.Genome query, properties.jaxb.Genome target) throws Exception {
        try(PreparedStatement preparedStatement=
                    this.connection.prepareStatement(
                            "select count(*) from gblaster.gco_view as `query` " +
                                    "inner join gblaster.blasts as `blasts` on query.id_orfs=blasts.orfs_id " +
                                    "inner join gblaster.gco_view as `target` on blasts.hitorf_id=target.id_orfs " +
                                    "where query.genome_name=? and target.genome_name=?")){
            preparedStatement.setString(1,query.getName().getName());
            preparedStatement.setString(2,target.getName().getName());
            final ResultSet resultSet=preparedStatement.executeQuery();
            resultSet.next();
            final int count=resultSet.getInt(1);
            if(count>0){
                return true;
            } else{
                return false;
            }
        }
    }

    public static GMySQLConnector get(String URL, String user, String password) {
        return new GMySQLConnector(URL, user, password);
    }
}

