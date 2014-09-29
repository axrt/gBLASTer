package blast.blast;

import blast.output.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.*;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by alext on 5/27/14.
 * TODO document class
 */
public final class BlastHelper {

    private BlastHelper() {
        throw new AssertionError("Helper class, non-instantiable.");
    }

    //Input query options
    public static final String QUERY = "-query";
    public static final String QUERY_LOC = "-query_loc";
    public static final String STRAND = "-strand";

    public static enum STRAND_VALS {
        BOTH("both"), MINUS("minus"), PLUS("plus");
        private String commandOption;//Default "both"

        private STRAND_VALS(String commandOption) {
            this.commandOption = commandOption;
        }

        @Override
        public String toString() {
            return this.commandOption;
        }
    }

    //General search options
    public static final String TASK = "-task";

    public static enum BLASTN_TASK_VALS {
        BLASTN("blastn"), BLASTN_SHORT("blastn-short"), DC_MEGABLAST("dc-megablast"),
        MEGABLAST("megablast"), RMBLASTN("rmblastn");
        private String commandOption;

        private BLASTN_TASK_VALS(String commandOption) {
            this.commandOption = commandOption;
        }

        @Override
        public String toString() {
            return this.commandOption;
        }
    }

    public static enum BLASTP_TASK_VALS {
        BLASTP("blastp"), BLASTP_SHORT("blastp-short"), DELTABLAST("deltablast");
        private String commandOption;

        private BLASTP_TASK_VALS(String commandOption) {
            this.commandOption = commandOption;
        }

        @Override
        public String toString() {
            return this.commandOption;
        }
    }

    public static final String DB = "-db";//Incompatible with:  subject, subject_loc
    public static final String OUT = "-out";
    public static final String EVALUE = "-evalue";//Default 10
    public static final String WORD_SIZE = "-word_size";
    public static final String GAPOPEN = "-gapopen";
    public static final String GAPEXTEND = "-gapextend";
    public static final String PENALTY = "-penalty";//Penalty for a nucleotide mismatch
    public static final String REWARD = "-reward";
    public static final String USE_INDEX = "-use_index";//Use MegaBLAST database index Default = "false"
    public static final String INDEX_NAME = "-index_name";//MegaBLAST database index name

    //BLAST-2-Sequences options
    public static final String SUBJECT = "-subject";/*Subject sequence(s) to search
    * Incompatible with:  db, gilist, seqidlist, negative_gilist,
    db_soft_mask, db_hard_mask*/
    public static final String SUBJECT_LOC = "-subject_loc";/*Location on the subject sequence in 1-based offsets (Format: start-stop)
    * Incompatible with:  db, gilist, seqidlist, negative_gilist,
    db_soft_mask, db_hard_mask, remote*/

    //Formatting options
    public static final String OUTFMT = "-outfmt";

    public static enum OUTFMT_VALS {
        PAIRWISE,
        QUERY_ANCHORED_SHOWING_IDENT,
        QUERY_ANCHORED_NO_IDENT,
        FLAT_QUERY_ANCHORED_SHOWING_IDENT,
        FLAT_QUERY_ANCHORED_NO_IDENT,
        XML,
        TABULAR,
        TABULAR_WITH_COMMENT_LINES,
        TEXT_ASN1,
        BINARY_ASN1,
        COMMA_SEP_VALS,
        BLAST_ARCHIVE_ASN1;

        @Override
        public String toString() {
            return String.valueOf(this.ordinal());
        }

        /**
         * Options 6, 7, and 10 can be additionally configured to produce
         * a custom format specified by space delimited format specifiers.
         * The supported format specifiers are:
         */
        public enum CUSTOM_FMT_VALS {
            qseqid,// means Query Seq-id
            qgi,// means Query GI
            qacc,// means Query accesion
            qaccver,// means Query accesion.version
            qlen,// means Query sequence length
            sseqid,// means Subject Seq-id
            sallseqid,// means All subject Seq-id(s), separated by a ';'
            sgi,// means Subject GI
            sallgi,// means All subject GIs
            sacc,// means Subject accession
            saccver,// means Subject accession.version
            sallacc,// means All subject accessions
            slen,// means Subject sequence length
            qstart,// means Start of alignment in query
            qend,// means End of alignment in query
            sstart,// means Start of alignment in subject
            send,// means End of alignment in subject
            qseq,// means Aligned part of query sequence
            sseq,// means Aligned part of subject sequence
            evalue,// means Expect value
            bitscore,// means comulativeBitScore score
            score,// means Raw score
            length,// means Alignment length
            pident,// means Percentage of identical matches
            nident,// means Number of identical matches
            mismatch,// means Number of mismatches
            positive,// means Number of positive-scoring matches
            gapopen,// means Number of gap openings
            gaps,//means Total number of gaps
            ppos,// means Percentage of positive-scoring matches
            frames,// means Query and subject frames separated by a '/'
            qframe,// means Query frame
            sframe,// means Subject frame
            btop,// means Blast traceback operations (BTOP)
            staxids,// means unique Subject Taxonomy ID(s), separated by a ';'(in numerical order)
            sscinames,// means unique Subject Scientific Name(s), separated by a ';'
            scomnames,// means unique Subject Common Name(s), separated by a ';'
            sblastnames,// means unique Subject Blast Name(s), separated by a ';' (in alphabetical order)
            sskingdoms,// means unique Subject Super Kingdom(s), separated by a ';'(in alphabetical order)
            stitle,// means Subject Title
            salltitles,// means All Subject Title(s), separated by a '<>'
            sstrand,// means Subject Strand
            qcovs,// means Query Coverage Per Subject
            qcovhsp,// means Query Coverage Per HSP
            /**
             * When not provided, the default value is:
             * 'qseqid sseqid pident length mismatch gapopen qstart qend sstart send
             * evalue bitscore', which is equivalent to the keyword 'std'
             * Default = `0'
             */
            std;

            @Override
            public String toString() {
                return this.name();
            }
        }

        public String toCommand() {
            return String.valueOf(this.ordinal());
        }

    }

    public static final String SHOW_GIS = "-show_gis";
    public static final String NUM_DESCRIPTIONS = "-num_descriptions";
    public static final String NUM_ALIGNMENTS = "-num_alignments";
    public static final String HTML = "-html";

    //Query filtering options
    public static final String DUST = "-dust";// Filter query sequence with DUST (Format: 'yes', 'level window linker', or 'no' to disable) Default = `20 64 1'

    public static enum DUST_VALS {
        YES("yes"), LEVEL_WINDOW_LINKER("level window linker"), NO("no");  //TODO needs a proper setter for values
        private String commandOption;//Default "both"

        private DUST_VALS(String commandOption) {
            this.commandOption = commandOption;
        }
    }

    public static final String FILTERING_DB = "-filtering_db";//BLAST database containing filtering elements (i.e.: repeats)
    public static final String WINDOW_MASKER_TAXID = "-window_masker_taxid";
    public static final String WINDOW_MASKER_DB = "-window_masker_db";
    public static final String SOFT_MASKING = "-soft_masking";//Default true;
    public static final String LCASE_MASKING = "-lcase_masking";

    //Restrict search or results
    public static final String GILIST = "-gilist";
    public static final String SEQIDLIST = "-seqidlist";
    public static final String NEGATIVE_GILIST = "-negative_gilist";
    public static final String CULLING_LIMIT = "-culling_limit";/*If the query range of a hit is enveloped by that of at least this many
    higher-scoring hits, delete the hit
    * Incompatible with:  best_hit_overhang, best_hit_score_edge*/
    public static final String DB_HARD_MASK = "-db_hard_mask";
    public static final String DB_SOFT_MASK = "-db_soft_mask";
    public static final String BEST_HIT_OVERHANG = "-best_hit_overhang";/*
    Best Hit algorithm overhang value (recommended value: 0.1)
    * Incompatible with:  culling_limit*/
    public static final String BEST_HIT_SCORE_EDGE = "-best_hit_score_edge";
    public static final String MAX_TARGET_SEQS = "-max_target_seqs";/*Maximum number of aligned sequences to keep
    Not applicable for outfmt <= 4 Default = `500'
    * Incompatible with:  num_descriptions, num_alignments*/

    //Discontiguous MegaBLAST options
    public static final String TEMPLATE_TYPE = "-template_type";

    public static enum TEMPLATE_TYPE_VALS {
        CODING("coding"), CODING_AND_OPTIMAL("coding_and_optimal"), OPTIMAL("optimal");
        private String commandOption;

        private TEMPLATE_TYPE_VALS(String commandOption) {
            this.commandOption = commandOption;
        }
    }//Requires:  template_length

    public static final String TEMPLATE_LENGTH = "-perc_identity";

    public static enum TEMPLATE_LENGTH_VALS {
        A("16"), B("18"), C("21");
        private String commandOption;

        private TEMPLATE_LENGTH_VALS(String commandOption) {
            this.commandOption = commandOption;
        }
    }//Requires:  template_type

    //Statistical options
    public static final String DBSIZE = "-dbsize";
    public static final String SEARCHSP = "-searchsp";
    public static final String MAX_HSPS = "-max_hsps";//Default hsp=0 -> no limit
    public static final String SUM_STATISTICS = "-sum_statistics";

    //Search strategy options
    public static final String IMPORT_SEARCH_STRATEGY = "-import_search_strategy";/*Search strategy to use
    * Incompatible with:  export_search_strategy*/
    public static final String EXPORT_SEARCH_STRATEGY = "-export_search_strategy";/*File name to record the search strategy used
    * Incompatible with:  import_search_strategy*/

    //Extension options
    public static final String XDROP_UNGAP = "-xdrop_ungap";
    public static final String XDROP_GAP_FINAL = "-xdrop_gap_final";
    public static final String MIN_RAW_GAPPED_SCORE = "-min_raw_gapped_score";
    public static final String UNGAPPED = "-ungapped";
    public static final String WINDOW_SIZE = "-window_size";
    public static final String OFF_DIAGONAL_RANGE = "-off_diagonal_range";//Number of off-diagonals to search for the 2nd hit, use 0 to turn off Default = '0'

    //Miscellaneous options
    public static final String PARSE_DEFLINES = "-parse_deflines";
    public static final String NUM_THREADS = "-num_threads";/*Number of threads (CPUs) to use in the BLAST search Default = `1'
    * Incompatible with:  remote*/
    public static final String REMOTE = "-remote";/*Execute search remotely?
    * Incompatible with:  gilist, seqidlist, negative_gilist, subject_loc,
    num_threads*/


    /**
     * Returns a {@link blast.output.BlastOutput} from an {@code InputStream}.
     *
     * @param in :{@link java.io.InputStream } from a URL or other type of connection
     * @return {@link blast.output.BlastOutput}
     * @throws javax.xml.bind.JAXBException
     */
    public static BlastOutput catchBLASTOutput(InputStream in)
            throws SAXException, JAXBException {
        final JAXBContext jc = JAXBContext.newInstance(BlastOutput.class);
        final Unmarshaller u = jc.createUnmarshaller();
        final XMLReader xmlreader = XMLReaderFactory.createXMLReader();
        xmlreader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlreader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                true);
        xmlreader.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                String file = null;
                if (systemId.contains("NCBI_BlastOutput.dtd")) {
                    file = "NCBI_BlastOutput.dtd";
                }
                if (systemId.contains("NCBI_Entity.mod.dtd")) {
                    file = "NCBI_Entity.mod.dtd";
                }
                if (systemId.contains("NCBI_BlastOutput.mod.dtd")) {
                    file = "NCBI_BlastOutput.mod.dtd";
                }
                return new InputSource(BlastOutput.class
                        .getResourceAsStream(file));
            }
        });
        final InputSource input = new InputSource(in);
        final Source source = new SAXSource(xmlreader, input);
        return (BlastOutput) u.unmarshal(source);
    }

    public static Optional<Iteration> unmarshallSingleIteraton(InputStream inputStream) throws JAXBException, SAXException {
        final JAXBContext jc = JAXBContext.newInstance(Iteration.class);
        final Unmarshaller u = jc.createUnmarshaller();
        final XMLReader xmlreader = XMLReaderFactory.createXMLReader();
        xmlreader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlreader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                true);
        xmlreader.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                String file = null;
                if (systemId.contains("NCBI_BlastOutput.dtd")) {
                    file = "NCBI_BlastOutput.dtd";
                }
                if (systemId.contains("NCBI_Entity.mod.dtd")) {
                    file = "NCBI_Entity.mod.dtd";
                }
                if (systemId.contains("NCBI_BlastOutput.mod.dtd")) {
                    file = "NCBI_BlastOutput.mod.dtd";
                }
                return new InputSource(BlastOutput.class
                        .getResourceAsStream(file));
            }
        });
        final InputSource input = new InputSource(inputStream);
        final Source source = new SAXSource(xmlreader, input);
        return Optional.of((Iteration) u.unmarshal(source));
    }

    public static Optional<HitHsps> unmarshallHsps(InputStream inputStream) throws JAXBException, SAXException {
        final JAXBContext jc = JAXBContext.newInstance(HitHsps.class);
        final Unmarshaller u = jc.createUnmarshaller();
        final XMLReader xmlreader = XMLReaderFactory.createXMLReader();
        xmlreader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlreader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                true);
        xmlreader.setEntityResolver(new EntityResolver() {

            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                String file = null;
                if (systemId.contains("NCBI_BlastOutput.dtd")) {
                    file = "NCBI_BlastOutput.dtd";
                }
                if (systemId.contains("NCBI_Entity.mod.dtd")) {
                    file = "NCBI_Entity.mod.dtd";
                }
                if (systemId.contains("NCBI_BlastOutput.mod.dtd")) {
                    file = "NCBI_BlastOutput.mod.dtd";
                }
                return new InputSource(BlastOutput.class
                        .getResourceAsStream(file));
            }
        });
        final InputSource input = new InputSource(inputStream);
        final Source source = new SAXSource(xmlreader, input);
        return Optional.of((HitHsps) u.unmarshal(source));
    }

    public static void marshallIteration(Iteration iteration, OutputStream outputStream) throws JAXBException {

        final JAXBContext jaxbContext = JAXBContext.newInstance(Iteration.class);
        final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(iteration, outputStream);

    }

    public static String marshallIterationToString(Iteration iteration) throws JAXBException {
        final StringWriter sw = new StringWriter();
        final JAXBContext jaxbContext = JAXBContext.newInstance(Iteration.class);
        final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        jaxbMarshaller.marshal(iteration, sw);
        return sw.toString();
    }

    public static int getPidents(Hit hit) {
        return hit.getHitHsps().getHsp().stream().mapToInt(hsp -> Integer.parseInt(hsp.getHspIdentity())).sum();
    }

    public static String getTabbedAlignment(Hit hit){
        final String queryAlnseq=hit.getHitHsps().getHsp().stream().map(hsp->hsp.getHspQseq()).collect(Collectors.joining());
        final String midline=hit.getHitHsps().getHsp().stream().map(hsp->hsp.getHspMidline()).collect(Collectors.joining());
        final String subjectAlnseq=hit.getHitHsps().getHsp().stream().map(hsp->hsp.getHspHseq()).collect(Collectors.joining());
        return queryAlnseq.concat("\t").concat(midline).concat("\t").concat(subjectAlnseq);
    }
    public static int getAlignmentLength(Hit hit){
        return hit.getHitHsps().getHsp().stream().mapToInt(hsp->Integer.parseInt(hsp.getHspAlignLen())).sum();
    }

    public static double comulativeBitScore(Iteration iteration) throws JAXBException, SAXException {

        final HitHsps hitHsps = iteration.getIterationHits().getHit().get(0).getHitHsps();
        return comulativeBitScore(hitHsps);
    }

    public static double comulativeBitScore(HitHsps hitHsps) throws JAXBException, SAXException {

        final double comulativeBitScore  = hitHsps.getHsp().stream().mapToDouble(hsp -> {
            return Double.parseDouble(hsp.getHspBitScore());
        }).sum();

        return comulativeBitScore;
    }
}
