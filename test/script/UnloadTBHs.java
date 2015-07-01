package script;

import analisys.bbh.TripledirectionalBlastHit;
import blast.blast.BlastHelper;
import blast.ncbi.output.HitHsps;
import db.GenomeDAO;
import db.ResearchDAO;
import db.derby.GDerbyEmbeddedResearchConnector;
import db.derby.GDerbyEmbeddedResearchConnectorTest;
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
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by alext on 6/30/15.
 */
public class UnloadTBHs {
    protected static GDerbyEmbeddedResearchConnector connector;
    final static Path toFolder = Paths.get("/home/alext/Documents/Research/gBLASTer/research");
    final static String HEADER="A_genome_id\tA_orf_id\tA_sequence\tA<->B_bitScore\t" +
            "B_genome_id\tB_orf_id\tB_sequence\tB<->C_bitScore\t" +
            "C_genome_id\tC_orf_id\tC_sequence\tC<->A_bitScore";

    public static void main (String[]args){

        if(!toFolder.toFile().exists()){
            toFolder.toFile().mkdir();
        }

        try {

            connector = GDerbyEmbeddedResearchConnector.
                    get("jdbc:derby:/home/alext/Documents/Research/gBLASTer/db/gblasterdb;create=true;",
                            "gblaster", "gblaster");
            connector.connectToDatabase();

            final ResearchDAO researchDAO=(ResearchDAO)connector;
            final GenomeDAO genomeDAO=(GenomeDAO)connector;

            //Create groups
            final String[]archaeas = {
                    "Methanosarcina_barkeri_str_Fusaro",
                    "Haloquadratum_walsbyi",
                    "Acidilobus_saccharovorans",
                    "Pyrobaculum_aerophilum_str_IM2"
            };

            final String[]eucaryotes = {
                   "Drosophila_simulans",
                    "Arabidopsis_thaliana",
                    "Ostreococcus_tauri",
                    "Takifugu_rubripes",
                    "Monosiga_brevicollis",
                    "Capsaspora_owczarzaki",
                    "Naegleria_fowleri",
                    "Caenorhabditis_Elegans_Bristol_N2",
                    "Saccharomyces_cerevisiae_S288c"
            };

            final String[]loki = {
               "Archaeon_Loki_Lokiarch"
            };

            final List<String[]> triplets=new ArrayList<>();

            for(int i=0;i<archaeas.length;i++){
                for(int j=0;j<eucaryotes.length;j++){
                    final String[]triplet=new String[3];
                    triplet[0]=archaeas[i];
                    triplet[1]=eucaryotes[j];
                    triplet[2]=loki[0];
                    triplets.add(triplet);
                }
            }

            triplets.stream().forEach(trp -> {
                System.out.print(trp[0]);
                System.out.print('\t');
                System.out.print(trp[1]);
                System.out.print('\t');
                System.out.println(trp[2]);
            });

            System.out.println("Number of triplets: "+triplets.size());

            for(String[]trp:triplets){
                System.out.println("Unloading: "+trp[0]+"->"+trp[1]+"->"+trp[2]);
                final Genome AGenome= GDerbyEmbeddedResearchConnectorTest.assembleMock(trp[0]);
                final Genome BGenome= GDerbyEmbeddedResearchConnectorTest.assembleMock(trp[1]);
                final Genome CGenome= GDerbyEmbeddedResearchConnectorTest.assembleMock(trp[2]);
                final Stream<TripledirectionalBlastHit> hitStream = researchDAO.getTBHForGenomes(AGenome, BGenome, CGenome, 100);

                final String name=""+genomeDAO.genomeIdByName(trp[0])+"_"
                        +genomeDAO.genomeIdByName(trp[1])+"_"
                        +genomeDAO.genomeIdByName(trp[2])
                        +".tbh";
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
                            String iter=tripleHit.getA().getTextIteration();


                                HitHsps hitHsps= BlastHelper.unmarshallHsps(
                                        IOUtils.toInputStream(
                                                iter.substring(
                                                        iter.indexOf("<Hit_hsps>"),iter.lastIndexOf("/<Hit_hsps>")
                                                )
                                        )
                                ).get();

                            bufferedWriter.write(""+BlastHelper.comulativeBitScore(hitHsps));
                            bufferedWriter.write('\t');

                            //B
                            bufferedWriter.write(String.valueOf(tripleHit.getB().getId_genomes()));
                            bufferedWriter.write('\t');
                            bufferedWriter.write(String.valueOf(tripleHit.getB().getOrfs_id()));
                            bufferedWriter.write('\t');
                            bufferedWriter.write(tripleHit.getB().getSequence());
                            bufferedWriter.write('\t');
                            iter=tripleHit.getB().getTextIteration();


                            hitHsps= BlastHelper.unmarshallHsps(
                                    IOUtils.toInputStream(
                                            iter.substring(
                                                    iter.indexOf("<Hit_hsps>"), iter.lastIndexOf("/<Hit_hsps>")
                                            )
                                    )
                            ).get();

                            bufferedWriter.write(""+BlastHelper.comulativeBitScore(hitHsps));
                            bufferedWriter.write('\t');

                            //C
                            bufferedWriter.write(String.valueOf(tripleHit.getC().getId_genomes()));
                            bufferedWriter.write('\t');
                            bufferedWriter.write(String.valueOf(tripleHit.getC().getOrfs_id()));
                            bufferedWriter.write('\t');
                            bufferedWriter.write(tripleHit.getC().getSequence());
                            bufferedWriter.write('\t');
                            iter=tripleHit.getC().getTextIteration();


                            hitHsps= BlastHelper.unmarshallHsps(
                                    IOUtils.toInputStream(
                                            iter.substring(
                                                    iter.indexOf("<Hit_hsps>"), iter.lastIndexOf("/<Hit_hsps>")
                                            )
                                    )
                            ).get();

                            bufferedWriter.write(""+BlastHelper.comulativeBitScore(hitHsps));
                            bufferedWriter.newLine();

                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        } catch (SAXException e) {
                            e.printStackTrace();
                        } catch (JAXBException e) {
                            e.printStackTrace();
                        }

                    });

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            //Assemble triplets
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
