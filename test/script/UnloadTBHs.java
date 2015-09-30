package script;

import analisys.bbh.TripledirectionalBlastHit;
import blast.blast.BlastHelper;
import blast.ncbi.output.HitHsps;
import db.GenomeDAO;
import db.ResearchDAO;
import db.derby.GDerbyEmbeddedResearchConnector;
import db.derby.GDerbyEmbeddedResearchConnectorTest;
import db.legend.GenomeLegend;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import properties.jaxb.Genome;

import javax.xml.bind.JAXBException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by alext on 6/30/15.
 */
public class UnloadTBHs {
    protected static GDerbyEmbeddedResearchConnector connector;
    final static Path toFolder = Paths.get("/home/alext/Documents/Research/gBLASTer/research");
    final static String HEADER = "A_genome_id\tA_orf_id\tA_sequence\tA<->B_bitScore\t" +
            "B_genome_id\tB_orf_id\tB_sequence\tB<->C_bitScore\t" +
            "C_genome_id\tC_orf_id\tC_sequence\tC<->A_bitScore";

    public static void main(String[] args) {

        if (!toFolder.toFile().exists()) {
            toFolder.toFile().mkdir();
        }

        try {

            connector = GDerbyEmbeddedResearchConnector.
                    get("jdbc:derby:/home/alext/Documents/Research/gBLASTer/db/gblasterdb;create=true;",
                            "gblaster", "gblaster");
            connector.connectToDatabase();

            final ResearchDAO researchDAO = (ResearchDAO) connector;
            final GenomeDAO genomeDAO = (GenomeDAO) connector;

            //Create groups
            final GenomeLegend legend=GenomeLegend.get(genomeDAO);
            final Map<Integer,String> idNameMap=new HashMap<>();
            for(int i=0;i<legend.size();i++){
                idNameMap.put(legend.get(i).getId(),legend.get(i).getName());
            }
            final List<String[]> triplets = new ArrayList<>();

            final int[] bacteria={1,2,3,28,33,36};
            final int loki=45;
            final int methano=16;
            for(int i=0;i<bacteria.length;i++){
                triplets.add(new String[]{idNameMap.get(bacteria[i]),idNameMap.get(loki),idNameMap.get(methano)});
            }

            triplets.stream().forEach(trp -> {
                System.out.print(trp[0]);
                System.out.print('\t');
                System.out.print(trp[1]);
                System.out.print('\t');
                System.out.println(trp[2]);
            });

            System.out.println("Number of triplets: " + triplets.size());

            for (String[] trp : triplets) {

                final Genome AGenome = GDerbyEmbeddedResearchConnectorTest.assembleMock(trp[0]);
                final Genome BGenome = GDerbyEmbeddedResearchConnectorTest.assembleMock(trp[1]);
                final Genome CGenome = GDerbyEmbeddedResearchConnectorTest.assembleMock(trp[2]);
                final Stream<TripledirectionalBlastHit> hitStream = researchDAO.getTBHForGenomes(AGenome, BGenome, CGenome, 100);

                final String name = "" + genomeDAO.genomeIdByName(trp[0]) + "_"
                        + genomeDAO.genomeIdByName(trp[1]) + "_"
                        + genomeDAO.genomeIdByName(trp[2])
                        + ".tbh";
                if (!toFolder.resolve(name).toFile().exists()) {
                    System.out.println("Unloading: " + trp[0] + "->" + trp[1] + "->" + trp[2] + " to "+ toFolder.resolve(name).toFile());

                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(toFolder.resolve(name).toFile()))) {

                        bufferedWriter.write(HEADER);
                        bufferedWriter.newLine();

                        hitStream.forEach(tripleHit -> {

                            try {
                                //A
                                bufferedWriter.write(String.valueOf(tripleHit.getA().getId_genomes()));
                                bufferedWriter.write('\t');
                                bufferedWriter.write(String.valueOf(tripleHit.getA().getOrfs_id()));
                                bufferedWriter.write('\t');
                                bufferedWriter.write(tripleHit.getA().getSequence());
                                bufferedWriter.write('\t');
                                String iter = tripleHit.getA().getTextIteration();


                                HitHsps hitHsps = BlastHelper.unmarshallHsps(
                                        IOUtils.toInputStream(
                                                iter.substring(
                                                        iter.indexOf("<Hit_hsps>"), iter.lastIndexOf("</Hit_hsps>") + "</Hit_hsps>".length()
                                                )
                                        )
                                ).get();

                                bufferedWriter.write("" + BlastHelper.comulativeBitScore(hitHsps));
                                bufferedWriter.write('\t');

                                //B
                                bufferedWriter.write(String.valueOf(tripleHit.getB().getId_genomes()));
                                bufferedWriter.write('\t');
                                bufferedWriter.write(String.valueOf(tripleHit.getB().getOrfs_id()));
                                bufferedWriter.write('\t');
                                bufferedWriter.write(tripleHit.getB().getSequence());
                                bufferedWriter.write('\t');
                                iter = tripleHit.getB().getTextIteration();


                                hitHsps = BlastHelper.unmarshallHsps(
                                        IOUtils.toInputStream(
                                                iter.substring(
                                                        iter.indexOf("<Hit_hsps>"), iter.lastIndexOf("</Hit_hsps>") + "</Hit_hsps>".length()
                                                )
                                        )
                                ).get();

                                bufferedWriter.write("" + BlastHelper.comulativeBitScore(hitHsps));
                                bufferedWriter.write('\t');

                                //C
                                bufferedWriter.write(String.valueOf(tripleHit.getC().getId_genomes()));
                                bufferedWriter.write('\t');
                                bufferedWriter.write(String.valueOf(tripleHit.getC().getOrfs_id()));
                                bufferedWriter.write('\t');
                                bufferedWriter.write(tripleHit.getC().getSequence());
                                bufferedWriter.write('\t');
                                iter = tripleHit.getC().getTextIteration();


                                hitHsps = BlastHelper.unmarshallHsps(
                                        IOUtils.toInputStream(
                                                iter.substring(
                                                        iter.indexOf("<Hit_hsps>"), iter.lastIndexOf("</Hit_hsps>") + "</Hit_hsps>".length()
                                                )
                                        )
                                ).get();

                                bufferedWriter.write("" + BlastHelper.comulativeBitScore(hitHsps));
                                bufferedWriter.newLine();

                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            } catch (SAXException e) {
                                e.printStackTrace();
                            } catch (JAXBException e) {
                                e.printStackTrace();
                            }

                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
            //Assemble triplets
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
