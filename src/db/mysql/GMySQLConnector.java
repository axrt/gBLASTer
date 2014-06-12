package db.mysql;

import db.GenomeDAO;
import sequence.nucleotide.genome.Chromosome;
import sequence.nucleotide.genome.Genome;
import sequence.nucleotide.genome.LargeChromosome;
import sequence.nucleotide.genome.LargeGenome;

import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by alext on 6/11/14.
 * TODO document class
 */
public class GMySQLConnector extends MySQLConnector implements GenomeDAO {

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
    public int saveLargeGenomeForName(LargeGenome genome) throws Exception {
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
    public boolean genomeForNameExists(String name) throws Exception {
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
    public int saveChromososmeForGenomeID(int genomeId, Chromosome chromosome) throws Exception {


        return 0;
    }

    @Override
    public int saveLargeChromososmeForGenomeID(int genomeId, LargeChromosome largeChromosome) throws Exception {
        int id_chormosome=0;
        try (PreparedStatement preparedStatement = this.connection
                .prepareStatement("INSERT INTO `gblaster`.`chromosomes` (`id_genome`, `name`, `sequence`) VALUES (?, ?, ?);",Statement.RETURN_GENERATED_KEYS);
             Reader reader = new InputStreamReader(largeChromosome.getSequenceInputstream())) {
            preparedStatement.setInt(1, genomeId);
            preparedStatement.setString(2,largeChromosome.getAc());
            preparedStatement.setCharacterStream(3, reader);
            preparedStatement.executeUpdate();
            ResultSet resultSet=preparedStatement.getGeneratedKeys();
            if(resultSet.next()){
                id_chormosome=resultSet.getInt(1);
            }
        }
        return id_chormosome;
    }

    @Override
    public Chromosome loadCrhomosomeForID(int id) throws Exception {
        return null;
    }

    @Override
    public LargeChromosome loadLargeCrhomosomeForID(int id) throws Exception {
        return null;
    }

    @Override
    public IntStream loadChromosomeIdsForGenomeId(int genomeID) throws Exception {
        try(PreparedStatement preparedStatement=this.connection
                .prepareStatement("select id_chromosomes from `gblaster`.`chromosomes` where id_genome=?")){
            preparedStatement.setInt(1,genomeID);
            final ResultSet resultSet=preparedStatement.executeQuery();
            final IntStream.Builder builder=IntStream.builder();
            while(resultSet.next()) {
                builder.accept(resultSet.getInt(1));
            }
            return builder.build();
        }
    }

    public static GMySQLConnector get(String URL, String user, String password) {
        return new GMySQLConnector(URL, user, password);
    }
}

