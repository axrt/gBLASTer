package script;

import blast.ncbi.output.HitHsps;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.*;

import java.nio.file.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alext on 8/1/14.
 * TODO document class
 */
public class ReparseBBHs {

    public static DecimalFormat DECIMAL_FORMAT=new DecimalFormat("#.00");

    @Test
    public void reparse() {
        final Path dir = Paths.get("/home/alext/Documents/gBlaster/bbh/");
        final Set<String> fileNames=Arrays.asList(dir.toFile().listFiles()).stream().map(file->{return file.getPath();}).collect(Collectors.toSet());
        final List<File> paths=new ArrayList<>();
        for(String s:fileNames){
            if(!s.endsWith(".short")&&!s.endsWith(".gz")){
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

   // @Test
    public void tryLine() {
        final String line = "74676\tVNFIVLVQYLIYTLRRYVRFNFMSEAKNLILATILSILIIVLWHIIYDNFLNTSQSQPSIENIEHIESSNDLAPMIYQNRSEIINSTREQRVNLTNNMLEGSISLKGARFDDLILTNYHLEPSSSSPQVVLLSPAESKDVYFAEFGWLDPNEKIKVPDSKTVWQADKLNQKEVNLFWDNENGILFRMKISLDDNYMFKVEQIIENNTKDNVVLVPYGKINRKRDNINESYWISHEGVLGAFNNKLEEWTYKDISKKRLIKASTSEKSWFGFADKYWFTAIIPEKSDQINVSIKHTNVNNIDKFQVDFVKPYKHILPGASVSTLNYFFAGAKKLNLLDSYKDTLNIPLFDKAVDFGVLYFITKPVFLLLEYFNFVLKNFGLAILLLTLVIKLLMLPLSNRSYISMFKVKSLQPELTRIKELYKNDSLKQHKETIALFKRNNVNPMSSIFPMLIQIPVFFALYKVLFVTIEMRHAPFYLWIKDLSASDPTNIFTLFGLFNYNFPISIGILPIIFGATMIIQQKLSEKDQTSKDDIQVNVMKFLPYISVFIFSSFPAGLVIYWIFSNIITLVQQSLIKLLLTRKVGMNVENTNS\t36664455\t<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Iteration><Iteration_iter-num>3</Iteration_iter-num><Iteration_query-ID>Query_3</Iteration_query-ID><Iteration_query-def>74676|0|2827|4599</Iteration_query-def><Iteration_query-len>591</Iteration_query-len><Iteration_hits><Hit><Hit_num>1</Hit_num><Hit_id>gnl|BL_ORD_ID|1965</Hit_id><Hit_def>11342796|1|138121|138615</Hit_def><Hit_accession>1965</Hit_accession><Hit_len>165</Hit_len><Hit_hsps><Hsp><Hsp_num>1</Hsp_num><Hsp_bit-score>26.9498</Hsp_bit-score><Hsp_score>58</Hsp_score><Hsp_evalue>1.52055</Hsp_evalue><Hsp_query-from>111</Hsp_query-from><Hsp_query-to>165</Hsp_query-to><Hsp_hit-from>106</Hsp_hit-from><Hsp_hit-to>158</Hsp_hit-to><Hsp_query-frame>0</Hsp_query-frame><Hsp_hit-frame>0</Hsp_hit-frame><Hsp_identity>18</Hsp_identity><Hsp_positive>30</Hsp_positive><Hsp_gaps>14</Hsp_gaps><Hsp_align-len>61</Hsp_align-len><Hsp_qseq>DDLILTNYHL--EPSSSSP-QVVLLSPAESKDVYFAEFGWLDPNE---KIKVPDSKTVWQA</Hsp_qseq><Hsp_hseq>EELVIQNYHLNPEPRGHRPGRLVRVAP--------GHYGWPHAREHGVNVGLPDGRHVNQA</Hsp_hseq><Hsp_midline>++L++NYHLEPP++V++P+GWE++PD+VQA</Hsp_midline></Hsp></Hit_hsps></Hit></Iteration_hits><Iteration_stat><Statistics><Statistics_db-num>3529</Statistics_db-num><Statistics_db-len>769850</Statistics_db-len><Statistics_hsp-len>84</Statistics_hsp-len><Statistics_eff-space>240020898</Statistics_eff-space><Statistics_kappa>0.041</Statistics_kappa><Statistics_lambda>0.267</Statistics_lambda><Statistics_entropy>0.14</Statistics_entropy></Statistics></Iteration_stat></Iteration>\t11342796\tYIGNTSQISSAAAQEQKPRGHPHGPEVAPRHNAPPPHGLHEPSQLLRRDPERLGRGAEVLGRRYGLDRPELLLAQAAAEGERQRHPLQGLLHGVNYQLPQLVGLNEELVIQNYHLNPEPRGHRPGRLVRVAPGHYGWPHAREHGVNVGLPDGRHVNQAGLDLDCV\t36635950\t<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Iteration><Iteration_iter-num>1966</Iteration_iter-num><Iteration_query-ID>Query_1966</Iteration_query-ID><Iteration_query-def>11342796|1|138121|138615</Iteration_query-def><Iteration_query-len>165</Iteration_query-len><Iteration_hits><Hit><Hit_num>1</Hit_num><Hit_id>gnl|BL_ORD_ID|2</Hit_id><Hit_def>74676|0|2827|4599</Hit_def><Hit_accession>2</Hit_accession><Hit_len>591</Hit_len><Hit_hsps><Hsp><Hsp_num>1</Hsp_num><Hsp_bit-score>26.9498</Hsp_bit-score><Hsp_score>58</Hsp_score><Hsp_evalue>0.27405</Hsp_evalue><Hsp_query-from>106</Hsp_query-from><Hsp_query-to>158</Hsp_query-to><Hsp_hit-from>111</Hsp_hit-from><Hsp_hit-to>165</Hsp_hit-to><Hsp_query-frame>0</Hsp_query-frame><Hsp_hit-frame>0</Hsp_hit-frame><Hsp_identity>18</Hsp_identity><Hsp_positive>30</Hsp_positive><Hsp_gaps>14</Hsp_gaps><Hsp_align-len>61</Hsp_align-len><Hsp_qseq>EELVIQNYHLNPEPRGHRPGRLVRVAP--------GHYGWPHAREHGVNVGLPDGRHVNQA</Hsp_qseq><Hsp_hseq>DDLILTNYHL--EPSSSSP-QVVLLSPAESKDVYFAEFGWLDPNE---KIKVPDSKTVWQA</Hsp_hseq><Hsp_midline>++L++NYHLEPP++V++P+GWE++PD+VQA</Hsp_midline></Hsp></Hit_hsps></Hit></Iteration_hits><Iteration_stat><Statistics><Statistics_db-num>1768</Statistics_db-num><Statistics_db-len>496981</Statistics_db-len><Statistics_hsp-len>70</Statistics_hsp-len><Statistics_eff-space>35455995</Statistics_eff-space><Statistics_kappa>0.041</Statistics_kappa><Statistics_lambda>0.267</Statistics_lambda><Statistics_entropy>0.14</Statistics_entropy></Statistics></Iteration_stat></Iteration>";


        try {
            System.out.println(parseString(line));
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        final Path testFile= Paths.get("/home/alext/Documents/gBlaster/bbh/Wolbachia_endosymbiont_of_Culex_quinquefasciatus_Pel_chromosome_complete_genome_VS_Acidilobus_saccharovorans");
        try {
            processFile(testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String parseString(String rawString) throws JAXBException, SAXException {


        final String[] split = rawString.split("\t");
        final String fwdBlast = split[3];
        final String rwdBlast = split[7];

        final String fwdHitHsps= fwdBlast.substring(
                fwdBlast.indexOf(
                        "<Hit_hsps>"), fwdBlast.lastIndexOf("</Hit_hsps>") + "</Hit_hsps>".length()
        );
        final String rwdHitHsps=rwdBlast.substring(
                fwdBlast.indexOf(
                        "<Hit_hsps>"), rwdBlast.lastIndexOf("</Hit_hsps>") + "</Hit_hsps>".length()
        );

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(split[0]);
        stringBuilder.append("\t");
        stringBuilder.append(split[1]);
        stringBuilder.append("\t");
        stringBuilder.append(split[2]);
        stringBuilder.append("\t");

        final double sumFwdScore =
                ReparseBHs.fastCommulativeBitScore(fwdHitHsps);

        final double sumRwdScore =
                ReparseBHs.fastCommulativeBitScore(rwdHitHsps);

        stringBuilder.append(DECIMAL_FORMAT.format(sumFwdScore));
        stringBuilder.append('\t');

        stringBuilder.append(ReparseBHs.processHitHsps(fwdHitHsps).trim());

        stringBuilder.append(split[4]);
        stringBuilder.append('\t');
        stringBuilder.append(split[5]);
        stringBuilder.append('\t');
        stringBuilder.append(split[6]);
        stringBuilder.append('\t');
        stringBuilder.append(DECIMAL_FORMAT.format(sumRwdScore));
        stringBuilder.append('\t');
        stringBuilder.append(ReparseBHs.processHitHsps(rwdHitHsps).trim());
        return stringBuilder.toString();

    }

    public static File processFile(Path in) throws IOException {
        final Path out = in.resolveSibling(in.getFileName().toString().concat(".short"));
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(in.toFile()));
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(out.toFile()))) {
             bufferedReader.lines().skip(1).forEach(line -> {

                 try {
                     bufferedWriter.write(parseString(line));
                     bufferedWriter.newLine();
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

    public static String processHitHsps(HitHsps hitHsps) {
        final StringBuilder stringBuilder = new StringBuilder();
        hitHsps.getHsp()
                .stream().forEach(hsp -> {
            stringBuilder.append("<Hsp_num>");
            stringBuilder.append(hsp.getHspNum());
            stringBuilder.append("</Hsp_num>");
            stringBuilder.append('\t');
            stringBuilder.append("<Hsp_qseq>");
            stringBuilder.append(hsp.getHspQseq());
            stringBuilder.append("</Hsp_qseq>");
            stringBuilder.append('\t');
            stringBuilder.append("<Hsp_hseq>");
            stringBuilder.append(hsp.getHspHseq());
            stringBuilder.append("</Hsp_hseq>");
            stringBuilder.append('\t');
            stringBuilder.append("<Hsp_midline>");
            stringBuilder.append(hsp.getHspMidline());
            stringBuilder.append("</Hsp_midline>");
            stringBuilder.append('\t');
        });
        return stringBuilder.toString();
    }

}
