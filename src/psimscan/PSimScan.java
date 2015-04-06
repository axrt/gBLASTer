package psimscan;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by alext on 4/6/15.
 */
public class PSimScan<R extends QSimScan.REPORT> extends QSimScan<R> {

    protected final Path toMatrixFile;

    protected PSimScan(PSimScanBuilder builder) {
        super(builder);
        this.toMatrixFile=builder.toMatrixFile;
    }

    public static class PSimScanBuilder extends QSimScanBuilder{
        /**
         * --matrix [BLOSUM62]: aminoacid substitution weight matrix to use (file name; matrix should be in NCBI format)
         */
        public static final String MATRIX="--matrix";

        protected final Path toMatrixFile;

        public PSimScanBuilder(Path queryFile, Path targetFile, Path outputFile, Path toMatrixFile) {
            super(queryFile, targetFile, outputFile);
            this.toMatrixFile=toMatrixFile;
            this.commandParams.add(MATRIX);
            this.commandParams.add(this.toMatrixFile.toFile().toString());
        }

        @Override
        public PSimScanBuilder appendOff() {
            super.appendOff();
            return this;
        }

        @Override
        public PSimScanBuilder appendOn() {
            super.appendOn();
            return this;
        }

        @Override
        public QSimScan build() {
            return null;
        }
    }

}
