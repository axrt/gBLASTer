package script;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by alext on 9/23/14.
 * TODO document class
 */
public class ReparseBHs {

    private static final double cutoff = 80;

    public static final Pattern BIT_SCORE_OPEN_TAG = Pattern.compile("<Hsp_bit-score>");
    public static final Pattern BIT_SCORE_CLOSE_TAG = Pattern.compile("</Hsp_bit-score>");
    public static final Pattern NUM_SCORE_OPEN_TAG = Pattern.compile("<Hsp_num>");
    public static final Pattern NUM_SCORE_CLOSE_TAG = Pattern.compile("</Hsp_num>");
    public static final Pattern QSEQ_SCORE_OPEN_TAG = Pattern.compile("<Hsp_qseq>");
    public static final Pattern QSEQ_SCORE_CLOSE_TAG = Pattern.compile("</Hsp_qseq>");
    public static final Pattern HSEQ_SCORE_OPEN_TAG = Pattern.compile("<Hsp_hseq>");
    public static final Pattern HSEQ_SCORE_CLOSE_TAG = Pattern.compile("</Hsp_hseq>");
    public static final Pattern MIDLINE_SCORE_OPEN_TAG = Pattern.compile("<Hsp_midline>");
    public static final Pattern MIDLINE_SCORE_CLOSE_TAG = Pattern.compile("</Hsp_midline>");


    protected static final String HEADER;

    static {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("QUERY_ORF_ID\t");
        stringBuilder.append("QUERY_SEQUENCE\t");
        stringBuilder.append("TARGET_ORF_ID\t");
        stringBuilder.append("TARGET_SEQUENCE\t");
        stringBuilder.append("BLAST_ID\t");
        stringBuilder.append("COMULATIVE_BITSCORE\t");
        stringBuilder.append("ITERATION\t");
        HEADER = stringBuilder.toString();
    }

    @Test
    public void reparse() {
        final Path dir = Paths.get("/home/alext/Documents/gBlaster/bh/");
        final Set<String> fileNames=Arrays.asList(dir.toFile().listFiles()).stream().map(file->{return file.getPath();}).collect(Collectors.toSet());
        final List<File> paths=new ArrayList<>();
        for(String s:fileNames){
            if(!s.endsWith(".short")){
                if(!fileNames.contains(s.concat(".short"))){
                   paths.add(new File(s));
                }
            }
        }

        paths.parallelStream().forEach(path -> {
            System.out.println("Parsing ".concat(path.getName().toString()));
            try {
                if (!path.getName().contains("directory")) {
                    processFile(path.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static String parseString(String s) throws JAXBException, SAXException {

        final String[] split = s.split("\t");
        final String iterationString = split[split.length - 1];
        final String report = iterationString.substring(
                iterationString.indexOf(
                        "<Hit_hsps>"), iterationString.lastIndexOf("</Hit_hsps>") + "</Hit_hsps>".length()
        );
        double comulativeBitScore = fastCommulativeBitScore(report);
        if (comulativeBitScore >= cutoff) {
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < split.length - 2; i++) {
                stringBuilder.append(split[i]);
                stringBuilder.append("\t");
            }
            stringBuilder.append(ReparseBBHs.DECIMAL_FORMAT.format(comulativeBitScore));
            stringBuilder.append("\t");
            stringBuilder.append(processHitHsps(report));
            return stringBuilder.toString();
        } else return "";
    }

    public static File processFile(Path in) throws IOException {
        final Path out = in.resolveSibling(in.getFileName().toString().concat(".short"));
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(in.toFile()));
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(out.toFile()))) {
            bufferedWriter.write(HEADER);
            bufferedWriter.newLine();

            bufferedReader.lines().skip(1).forEach(line -> {

                try {
                    final String toWrite = parseString(line);
                    if (!toWrite.equals("")) {
                        bufferedWriter.write(toWrite);
                        bufferedWriter.newLine();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JAXBException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }

            });
        }
        return in.toFile();
    }

    public static String processHitHsps(String source) {
        final StringBuilder stringBuilder = new StringBuilder();

        final List<Matcher>matchers=Arrays.asList(new Matcher[]{
                NUM_SCORE_OPEN_TAG.matcher(source),
                NUM_SCORE_CLOSE_TAG.matcher(source),
                QSEQ_SCORE_OPEN_TAG.matcher(source),
                QSEQ_SCORE_CLOSE_TAG.matcher(source),
                HSEQ_SCORE_OPEN_TAG.matcher(source),
                HSEQ_SCORE_CLOSE_TAG.matcher(source),
                MIDLINE_SCORE_OPEN_TAG.matcher(source),
                MIDLINE_SCORE_CLOSE_TAG.matcher(source)});

        while(matchers.get(0).find()){

            matchers.stream().skip(1).forEach(Matcher::find);
            int i=0;

            stringBuilder.append("<Hsp_num>");
            stringBuilder.append(source.substring(matchers.get(i++).end(),matchers.get(i++).start()));
            stringBuilder.append("</Hsp_num>");
            stringBuilder.append('\t');
            stringBuilder.append("<Hsp_qseq>");
            stringBuilder.append(source.substring(matchers.get(i++).end(), matchers.get(i++).start()));
            stringBuilder.append("</Hsp_qseq>");
            stringBuilder.append('\t');
            stringBuilder.append("<Hsp_hseq>");
            stringBuilder.append(source.substring(matchers.get(i++).end(), matchers.get(i++).start()));
            stringBuilder.append("</Hsp_hseq>");
            stringBuilder.append('\t');
            stringBuilder.append("<Hsp_midline>");
            stringBuilder.append(source.substring(matchers.get(i++).end(), matchers.get(i++).start()));
            stringBuilder.append("</Hsp_midline>");
            stringBuilder.append('\t');
        }

        return stringBuilder.toString();
    }

    public static double fastCommulativeBitScore(String source) {

        final Matcher openMatcher = BIT_SCORE_OPEN_TAG.matcher(source);
        final Matcher closeMatcher = BIT_SCORE_CLOSE_TAG.matcher(source);
        double comulativeBitScore = 0;
        while (openMatcher.find()) {
            closeMatcher.find();
            comulativeBitScore += Double.parseDouble(source.substring(openMatcher.end(), closeMatcher.start()));
        }

        return comulativeBitScore;
    }
}
