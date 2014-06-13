package blast.db;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Created by alext on 6/13/14.
 * TODO document class
 */
public final class MakeBlastDB implements Callable<Optional<File>>{

    public static final String MAKEBLASTDB="makeblastdb";
    protected final Path makeBlastDb;
    protected final List<String>command;
    protected final Path pathToDbFolder;
    protected final Path sequenceFile;
    protected final String name;

    protected MakeBlastDB(MakeBlastDBBuilder builder) {
        this.makeBlastDb=builder.makeBlastDb;
        this.command = builder.command;
        this.pathToDbFolder = builder.pathToDbFolder;
        this.sequenceFile = builder.pathToSequenceFile;
        this.name = builder.name;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Optional<File> call() throws Exception {
        return null;
    }
    public static class MakeBlastDBBuilder{
        private Path makeBlastDb;
        private Path pathToDbFolder;
        private Path pathToSequenceFile;
        private String name;
        private final ArrayList<String> command;

        public MakeBlastDBBuilder() {
            this.command=new ArrayList<>();
        }

        public MakeBlastDBBuilder pathToMakeBlastDb(Path toMakeBlastDb){
            this.makeBlastDb=toMakeBlastDb;
            return this;
        }
        public MakeBlastDBBuilder pathToDbFolder(Path toDbFolder){
            this.pathToDbFolder=toDbFolder;
            return this;
        }
        public MakeBlastDBBuilder pathToSequenceFile(Path toSequenceFile){
            this.pathToSequenceFile=toSequenceFile;
            return this;
        }
        public MakeBlastDBBuilder name(String name){
            this.name=name;
            return this;
        }
        public MakeBlastDB build(){
            return new MakeBlastDB(this);
        }
    }
}
