package blast.db;

import gblaster.blast.GPUGBlast;

import java.nio.file.Path;

/**
 * Created by alext on 10/9/14.
 */
public class GPUMakeBlastDB extends MakeBlastDB {


    protected GPUMakeBlastDB(GPUMakeBlastDBBuilder builder) {
        super(builder);
    }

    public static class GPUMakeBlastDBBuilder extends MakeBlastDBBuilder{

        public GPUMakeBlastDBBuilder(String name) {
            super(name);
        }

        @Override
        public GPUMakeBlastDBBuilder pathToMakeBlastDb(Path toMakeBlastDb) {
            return (GPUMakeBlastDBBuilder)super.pathToMakeBlastDb(toMakeBlastDb);
        }

        @Override
        public GPUMakeBlastDBBuilder pathToDbFolder(Path toDbFolder) {
            return (GPUMakeBlastDBBuilder)super.pathToDbFolder(toDbFolder);
        }

        @Override
        public GPUMakeBlastDBBuilder type(DBType type) {
            return (GPUMakeBlastDBBuilder)super.type(type);
        }

        @Override
        public GPUMakeBlastDBBuilder pathToSequenceFile(Path toSequenceFile) {
            return (GPUMakeBlastDBBuilder)super.pathToSequenceFile(toSequenceFile);
        }

        @Override
        public GPUMakeBlastDBBuilder name(String name) {
            return (GPUMakeBlastDBBuilder)super.name(name);
        }

        public GPUMakeBlastDB build() {

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
            this.command.add("-sort_volumes");

            return new GPUMakeBlastDB(this);
        }
    }
}
