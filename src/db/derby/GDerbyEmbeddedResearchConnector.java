package db.derby;

import analisys.bbh.BlastHit;
import analisys.bbh.TripledirectionalBlastHit;
import db.ResearchDAO;
import properties.jaxb.Genome;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by alext on 6/30/15.
 */
public class GDerbyEmbeddedResearchConnector extends GDerbyEmbeddedConnector implements ResearchDAO {


    /**
     * @param URL      {@link String} of the database
     * @param user     {@link String} user name
     * @param password {@link String} password
     */
    protected GDerbyEmbeddedResearchConnector(String URL, String user, String password) {
        super(URL, user, password);
    }

    public static GDerbyEmbeddedResearchConnector get(String URL, String user, String password) {
        return new GDerbyEmbeddedResearchConnector(URL, user, password);
    }

    @Override
    public Stream<TripledirectionalBlastHit> getTBHForGenomes(Genome a, Genome b, Genome c, int balancer) throws Exception {

        final int Agenome = this.genomeIdByName(a.getName().getName());
        final int Bgenome = this.genomeIdByName(b.getName().getName());
        final int Cgenome = this.genomeIdByName(c.getName().getName());

        try {
            //18,45,45,18,18,8,8,18,8,45,45,8 ->AA,BB,CC,DD,EE,FF -> query, target
            final PreparedStatement preparedStatement =
                    this.connection.prepareStatement(
                            "select\n" +
                                    "A.id_query_genome, A.id_blast, A.id_query_orf, AO.sequence, A.id_target_orf, A.iteration,\n" +
                                    "B.id_query_genome, B.id_blast, B.id_query_orf, BO.sequence, B.id_target_orf, B.iteration,\n" +
                                    "D.id_query_genome, D.id_blast, D.id_query_orf, DO.sequence, D.id_target_orf, D.iteration\n" +
                                    "from\n" +
                                    "app.blasts A\n" +
                                    "inner join app.blasts B on A.id_query_orf = B.id_target_orf and A.id_target_orf = B.id_query_orf\n" +
                                    "inner join app.blasts C on A.id_query_orf = C.id_query_orf\n" +
                                    "inner join app.blasts D on C.id_query_orf = D.id_target_orf and C.id_target_orf = D.id_query_orf\n" +
                                    "inner join app.blasts E on D.id_query_orf = E.id_query_orf\n" +
                                    "inner join app.blasts F on E.id_query_orf = F.id_target_orf and E.id_target_orf = F.id_query_orf\n" +
                                    "inner join app.orfs AO on A.id_query_orf = AO.id_orf\n" +
                                    "inner join app.orfs BO on B.id_query_orf = BO.id_orf\n" +
                                    "inner join app.orfs DO on D.id_query_orf = DO.id_orf\n" +
                                    "where F.id_query_orf=B.id_query_orf\n" +
                                    "and A.id_query_genome=?\n" +
                                    "and A.id_target_genome=?\n" +
                                    "and B.id_query_genome=?\n" +
                                    "and B.id_target_genome=?\n" +
                                    "and C.id_query_genome=?\n" +
                                    "and C.id_target_genome=?\n" +
                                    "and D.id_query_genome=?\n" +
                                    "and D.id_target_genome=?\n" +
                                    "and E.id_query_genome=?\n" +
                                    "and E.id_target_genome=?\n" +
                                    "and F.id_query_genome=?\n" +
                                    "and F.id_target_genome=?",
                            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            int setter = 0;
            preparedStatement.setInt(++setter, Agenome);
            preparedStatement.setInt(++setter, Bgenome);
            preparedStatement.setInt(++setter, Bgenome);
            preparedStatement.setInt(++setter, Agenome);
            preparedStatement.setInt(++setter, Agenome);
            preparedStatement.setInt(++setter, Cgenome);
            preparedStatement.setInt(++setter, Cgenome);
            preparedStatement.setInt(++setter, Agenome);
            preparedStatement.setInt(++setter, Cgenome);
            preparedStatement.setInt(++setter, Bgenome);
            preparedStatement.setInt(++setter, Bgenome);
            preparedStatement.setInt(++setter, Cgenome);
            preparedStatement.setFetchSize(balancer);

            final ResultSet resultSet = preparedStatement.executeQuery();

            Iterator<TripledirectionalBlastHit> iter = new Iterator<TripledirectionalBlastHit>() {
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
                public TripledirectionalBlastHit next() {
                    try {
                        int selector = 0;
                        final BlastHit blastHitA;

                        blastHitA = BlastHit.get(
                                resultSet.getInt(++selector), resultSet.getLong(++selector),
                                resultSet.getLong(++selector), resultSet.getString(++selector),
                                resultSet.getLong(++selector), resultSet.getString(++selector));

                        final BlastHit blastHitB;
                        blastHitB = BlastHit.get(
                                resultSet.getInt(++selector), resultSet.getLong(++selector),
                                resultSet.getLong(++selector), resultSet.getString(++selector),
                                resultSet.getLong(++selector), resultSet.getString(++selector));

                        final BlastHit blastHitC;
                        blastHitC = BlastHit.get(
                                resultSet.getInt(++selector), resultSet.getLong(++selector),
                                resultSet.getLong(++selector), resultSet.getString(++selector),
                                resultSet.getLong(++selector), resultSet.getString(++selector));

                        return new TripledirectionalBlastHit(blastHitA, blastHitB, blastHitC);
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
}
