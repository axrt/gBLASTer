package script;

import blast.blast.BlastHelper;
import blast.ncbi.output.HitHsps;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Created by alext on 9/8/14.
 * TODO document class
 */
public class ParseBHs {

    public static void main(String[] args) {
        final Path input = Paths.get("/home/alext/Backup/db/mysql/bhs/blast.bh");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(input.toFile()));
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(input.resolveSibling("scores.table").toFile()))) {

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");

                if (line.endsWith("<end>")) {

                    final String record = stringBuilder.toString();

                    final String blastIdString = record.substring(0, record.indexOf('\t'));
                    final String scoreValue = String.valueOf(comulativeScore(record.substring(record.indexOf('\"')+1, record.lastIndexOf('\"')).replaceAll("\\\\","")));
                    bufferedWriter.write(blastIdString.concat("\t").concat(scoreValue));
                    bufferedWriter.newLine();
                    stringBuilder = new StringBuilder();
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }


    public static double comulativeScore(String xmlBlastOutput) throws JAXBException, SAXException {
        xmlBlastOutput=xmlBlastOutput.substring(xmlBlastOutput.indexOf("<Hit_hsps>"),xmlBlastOutput.indexOf("</Hit_hsps>")+"</Hit_hsps>".length());
        final InputStream stream = new ByteArrayInputStream(xmlBlastOutput.getBytes(StandardCharsets.UTF_8));
        final Optional<HitHsps> hitHsps = BlastHelper.unmarshallHsps(stream);
        final double comulativeScore;
        if (hitHsps.isPresent()) {

            comulativeScore = hitHsps.get().getHsp().stream().mapToDouble(hsp -> {
                return Double.parseDouble(hsp.getHspBitScore());
            }).sum();
        } else {
            comulativeScore = 0;
        }

        return comulativeScore;
    }

}
