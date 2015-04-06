package gblaster.blast;

import blast.blast.BlastHelper;
import blast.ncbi.output.BlastOutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by alext on 10/9/14.
 */
public class GPUGBlast extends GBlast {

    public static final String GPU = "-gpu";
    public static final String GPU_THREADS = "-gpu_threads";
    public static final String GPU_BLOCKS = "-gpu_blocks";
    protected static final String GPU_MARK = ".gpu";
    protected final File markerFile;

    public enum Method {
        ALIGNMENT(1), CREATE_DATABASE(2);
        private final int mode;

        private Method(int mode) {
            this.mode = mode;
        }

        public String getMode() {
            return String.valueOf(this.mode);
        }

        public static final String METHOD = "-method";
    }

    protected GPUGBlast(GPUGBlastBuilder builder) {
        super(builder);
        this.markerFile = builder.getMarkerFile();
    }

    @Override
    public Optional<BlastOutput> call() throws Exception {
        if (!this.markerFile.exists()) {
            final List<String> modeFormat = new ArrayList<>();
            for (int i = 0; i < this.command.size(); i++) {
                if (!this.command.get(i).contains(BlastHelper.NUM_THREADS)) {
                    modeFormat.add(this.command.get(i));
                } else {
                    i++;
                }
            }
            modeFormat.add(Method.METHOD);
            modeFormat.add(Method.CREATE_DATABASE.getMode());
            synchronized (System.out.getClass()) {
                System.out.println(modeFormat.stream().collect(Collectors.joining(" ")));
            }
            final ProcessBuilder processBuilder = new ProcessBuilder(modeFormat);
            final Process p = processBuilder.start();
            try (InputStream inputStream = p.getInputStream();
                 InputStream errorStream = p.getErrorStream();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(errorStream))) {
                bufferedReader.lines().forEach(l -> {
                    synchronized (System.out.getClass()) {
                        System.out.println("BLAST ERR METHOD 2:> ".concat(l));
                    }
                });
            }
        }

        return super.call();
    }

    public static class GPUGBlastBuilder extends GBlastPBuilder {


        public GPUGBlastBuilder(Path pathToBlast, Path queryFile, String database) {
            super(pathToBlast, queryFile, database);
        }

        public GPUGBlastBuilder gpuThreads(int numThreads) {
            if (numThreads < 1 || numThreads > 1024) {
                throw new IllegalArgumentException(GPU_THREADS + " must be within [1..1024]!");
            }
            this.optionalParams.put(GPU_THREADS, String.valueOf(numThreads));
            return this;
        }

        public GPUGBlastBuilder gpuBlocks(int numBlocks) {
            if (numBlocks < 1 || numBlocks > 65536) {
                throw new IllegalArgumentException(GPU_BLOCKS + " must be within [1..65536]!");
            }
            this.optionalParams.put(GPU_BLOCKS, String.valueOf(numBlocks));
            return this;
        }

        public File getMarkerFile() {
            return new File(this.queryFile.toFile(), GPU_MARK);
        }

        @Override
        public GBlast build() {
            this.optionalParams.put(GPU, "T");
            return new GPUGBlast(this);
        }
    }
}
