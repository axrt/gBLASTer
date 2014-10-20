package blast.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created by alext on 6/13/14.
 * TODO document class
 */
public class MakeBlastDB implements Callable<Optional<File>> {

    public static final String MAKEBLASTDB = "makeblastdb";

    public enum DBType {
        NUCL("nucl"), PROT("prot");
        private String type;

        DBType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    protected final Path makeBlastDb;
    protected final List<String> command;
    protected final Path pathToDbFolder;
    protected final Path sequenceFile;
    protected final String name;
    protected final DBType type;

    protected MakeBlastDB(MakeBlastDBBuilder builder) {
        this.makeBlastDb = builder.makeBlastDb;
        this.command = builder.command;
        this.pathToDbFolder = builder.pathToDbFolder;
        this.sequenceFile = builder.pathToSequenceFile;
        this.name = builder.name;
        this.type = builder.type;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Optional<File> call() throws Exception {
        synchronized (System.out.getClass()) {
            System.out.println(this.command.stream().collect(Collectors.joining(" ")));
        }
        final ProcessBuilder processBuilder = new ProcessBuilder(this.command);
        final Process p = processBuilder.start();
        try (InputStream inputStream = p.getInputStream();
             InputStream errorStream = p.getErrorStream();
             BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(errorStream));
             BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(inputStream))) {
             System.out.println(inputStreamReader.lines().collect(Collectors.joining("\n","OUT>: ","")));
             System.out.println(errorStreamReader.lines().collect(Collectors.joining("\n","ERR>: ","")));
        }

        return Optional.of(this.pathToDbFolder.resolve(this.name).toFile());
    }

    public static class MakeBlastDBBuilder {
        protected Path makeBlastDb;
        protected Path pathToDbFolder;
        protected Path pathToSequenceFile;
        protected String name;
        protected DBType type;
        protected final ArrayList<String> command;

        public MakeBlastDBBuilder(String name) {
            this.name = name;
            this.command = new ArrayList<>();
        }

        public MakeBlastDBBuilder pathToMakeBlastDb(Path toMakeBlastDb) {
            this.makeBlastDb = toMakeBlastDb;
            return this;
        }

        public MakeBlastDBBuilder pathToDbFolder(Path toDbFolder) {
            this.pathToDbFolder = toDbFolder;
            return this;
        }

        public MakeBlastDBBuilder type(DBType type) {
            this.type = type;
            return this;
        }

        public MakeBlastDBBuilder pathToSequenceFile(Path toSequenceFile) {
            this.pathToSequenceFile = toSequenceFile;
            return this;
        }

        public MakeBlastDBBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MakeBlastDB build() {
            if (!pathChecks(this.makeBlastDb) || !pathChecks(this.pathToDbFolder) || !pathChecks(this.pathToSequenceFile)) {
                throw new IllegalStateException("One of the input paths is null or does not exist. Please check!");
            }
            if (this.type == null) {
                throw new IllegalArgumentException("Please specify DBType (-nucl,-prot) in builder.");
            }
            this.command.add(this.makeBlastDb.toFile().getPath());
            this.command.add("-in");
            this.command.add(this.pathToSequenceFile.toFile().getPath());
            this.command.add("-dbtype");
            this.command.add(this.type.getType());
            this.command.add("-out");
            this.command.add(this.pathToDbFolder.resolve(this.name).toFile().getPath());

            return new MakeBlastDB(this);
        }

        protected static boolean pathChecks(Path toFile) {
            if (toFile == null || !toFile.toFile().exists()) {
                return false;
            }
            return true;
        }
    }
}
