package script;

import blast.blast.BlastHelper;
import blast.ncbi.output.HitHsps;
import blast.ncbi.output.Hsp;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/22/15.
 */
public class BitScoreCorrection {

    public static void main(String[] args) {

        //tmp
        args = new String[3];
        args[0] = "/home/alext/Documents/Research/gBLASTer/bh/10_VS_9_80.0";
        args[1] = "/home/alext/Documents/Research/gBLASTer/bh/10_VS_9_80.0.fix";
        args[2] = "1";

        final Path toInputFile = Paths.get(args[0]);
        final Path toOutputFile = Paths.get(args[1]);
        final Double cutoff = Double.parseDouble(args[2]);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(toOutputFile.toFile()))) {
            final List<String> scoresList = Files.lines(toInputFile).map(line -> {
                if (line.startsWith("QUERY_ORF_ID")) {
                    return line;
                } else {
                    try {
                        return ReparseBBHs.DECIMAL_FORMAT.format(parseStringForBitScore(line));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).collect(Collectors.toList());

            final int[] counter={0};
            Files.lines(toInputFile).forEach(line -> {
                try {
                    if (line.startsWith("QUERY_ORF_ID")) {

                        writer.write(line);

                    } else {
                        final String[]stringSplit=line.split("\t");
                        for(int i=0;i<5;i++){
                            writer.write(stringSplit[0]);
                            writer.write("\t");
                        }
                        writer.write(scoresList.get(counter[0]));
                        writer.write("\t");
                        writer.write(stringSplit[6]);
                        counter[0]++;
                    }
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double parseStringForBitScore(String line) throws JAXBException, SAXException {

        final String[] lineSplit = line.split("\t");
        final InputStream inputStream = IOUtils.toInputStream(lineSplit[6].substring(lineSplit[6].indexOf("<Hit_hsps>"), lineSplit[6].indexOf("</Hit_hsps>") + "</Hit_hsps>".length()));
        final Optional<HitHsps> hitHspsOpt = BlastHelper.unmarshallHsps(inputStream);
        final HitHsps hitHsps = hitHspsOpt.get();

        return BlastHelper.comulativeBitScore(hitHsps);

    }

}
