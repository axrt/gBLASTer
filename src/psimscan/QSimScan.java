package psimscan;

import format.BadFormatException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by alext on 4/6/15.
 */
public abstract class QSimScan<R extends QSimScan.REPORT> implements Callable<List<R>> {

    protected final Path queryFile;
    protected final Path targetFile;
    protected final Path outputFile;
    protected final int numberFastasPerPart;
    protected List<Path> inputParts;
    protected final List<String> commandParams;

    protected QSimScan(QSimScanBuilder builder) {
        this.queryFile=builder.queryFile;
        this.targetFile=builder.targetFile;
        this.outputFile=builder.outputFile;
        this.numberFastasPerPart=builder.numberFastasPerPart;
        this.commandParams=builder.commandParams;
    }

    @Override
    public List<R> call() throws Exception {

        this.inputParts=QSimScanHelper.splitFasta(this.queryFile,this.numberFastasPerPart);

        for(Path p:this.inputParts){
            final List<String>command=new ArrayList<>();
            command.add(p.toFile().toString());
            command.addAll(this.commandParams);
            command.add(this.targetFile.toFile().toString());
            command.add(this.outputFile.toFile().toString());
            final ProcessBuilder processBuilder=new ProcessBuilder(command);
            final Process process=processBuilder.start();
            process.waitFor();
        }

        return new ArrayList<>();
    }

    public abstract static class QSimScanBuilder {
        /**
         * -a --ap --append : Append results to existing output object (or file)
         */
        protected static final String APPEND ="--append";

        protected final Path queryFile;
        protected final Path targetFile;
        protected final Path outputFile;
        protected int numberFastasPerPart=1000;
        protected final List<String> commandParams;

        public QSimScanBuilder(Path queryFile, Path targetFile, Path outputFile) {

            this.queryFile = queryFile;
            this.targetFile = targetFile;
            this.outputFile = outputFile;
            this.commandParams=new ArrayList<>();
            this.appendOn();//default

        }

        public QSimScanBuilder setNumberFastasPerPart(int numberOfParts) {
            this.numberFastasPerPart = numberOfParts;
            return this;
        }

        public QSimScanBuilder appendOn(){
            if(!this.commandParams.contains(APPEND)){
                this.commandParams.add(APPEND);
            }
            return this;
        }

        public QSimScanBuilder appendOff(){
            if(this.commandParams.contains(APPEND)){
                this.commandParams.remove(APPEND);
            }
            return this;
        }

        public abstract QSimScan build();

    }

    public abstract static class REPORT {

    }

    public static class TABX extends REPORT {
        protected final String Q_id;
        protected final String S_id;
        protected final double p_inden;
        protected final int al_len;
        protected final int mism;
        protected final int gaps;
        protected final int gap_len;
        protected final int qry_beg;
        protected final int qry_end;
        protected final int qry_len;
        protected final int trg_beg;
        protected final int trg_end;
        protected final int trg_len;
        protected final double evalue;
        protected final int sw_score;
        protected final int qry_auto;
        protected final int trg_auto;
        protected final String CIGAR;

        protected TABX(String[] line) {

            int i = 0;
            this.Q_id = line[i++];
            this.S_id = line[i++];
            this.p_inden = Double.valueOf(line[i++]);
            this.al_len = Integer.valueOf(line[i++]);
            this.mism = Integer.valueOf(line[i++]);
            this.gaps = Integer.valueOf(line[i++]);
            this.gap_len = Integer.valueOf(line[i++]);
            this.qry_beg = Integer.valueOf(line[i++]);
            this.qry_end = Integer.valueOf(line[i++]);
            this.qry_len = Integer.valueOf(line[i++]);
            this.trg_beg = Integer.valueOf(line[i++]);
            this.trg_end = Integer.valueOf(line[i++]);
            this.trg_len = Integer.valueOf(line[i++]);
            this.evalue = Double.valueOf(line[i++]);
            this.sw_score = Integer.valueOf(line[i++]);
            this.qry_auto = Integer.valueOf(line[i++]);
            this.trg_auto = Integer.valueOf(line[i++]);
            this.CIGAR = line[i];

        }

        public String getQ_id() {
            return Q_id;
        }

        public String getS_id() {
            return S_id;
        }

        public double getP_inden() {
            return p_inden;
        }

        public int getAl_len() {
            return al_len;
        }

        public int getMism() {
            return mism;
        }

        public int getGaps() {
            return gaps;
        }

        public int getGap_len() {
            return gap_len;
        }

        public int getQry_beg() {
            return qry_beg;
        }

        public int getQry_end() {
            return qry_end;
        }

        public int getQry_len() {
            return qry_len;
        }

        public int getTrg_beg() {
            return trg_beg;
        }

        public int getTrg_end() {
            return trg_end;
        }

        public int getTrg_len() {
            return trg_len;
        }

        public double getEvalue() {
            return evalue;
        }

        public int getSw_score() {
            return sw_score;
        }

        public int getQry_auto() {
            return qry_auto;
        }

        public int getTrg_auto() {
            return trg_auto;
        }

        public String getCIGAR() {
            return CIGAR;
        }

        public static TABX fromLine(String line) throws BadFormatException {
            //TODO implement the actual "bad" format
            return new TABX(line.split("\t"));
        }
    }

}
