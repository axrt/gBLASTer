package blast.blast;

import blast.output.BlastOutput;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static blast.blast.BlastHelper.*;

/**
 * Created by alext on 5/27/14.
 * TODO document class
 */
public abstract class AbstractBlast<E> implements Callable<Optional<BlastOutput>> {
    protected List<BlastEventListner<E>> listeners;

    protected AbstractBlast() {
        this.listeners = new ArrayList<>();
    }

    public abstract static class BlastBuilder {
        protected final Path pathToBlast;
        protected final Path queryFile;
        protected final String database;
        protected final Map<String, String> optionalParams;

        public BlastBuilder(Path pathToBlast, Path queryFile, String database) {
            this.pathToBlast = pathToBlast;
            this.database = database;
            this.queryFile = queryFile;
            this.optionalParams = new LinkedHashMap<>();
        }
        protected abstract void die(Optional<Stream<String>> params, String addition);

        public BlastBuilder query_loc(Optional<String> value) {
            value.ifPresent(v -> this.optionalParams.put(QUERY_LOC, v));
            return this;
        }
        public BlastBuilder out(Optional<Path>value){
            value.ifPresent(v -> this.optionalParams.put(OUT, v.toFile().getPath()));
            return this;
        }
        public <T>BlastBuilder task(Optional<T> value) {
            value.ifPresent(v -> this.optionalParams.put(TASK, v.toString()));
            return this;
        }
        public BlastBuilder evalue(Optional<Double> value) {
            value.ifPresent(v -> this.optionalParams.put(EVALUE, v.toString()));
            return this;
        }
        public BlastBuilder word_size(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(WORD_SIZE, v.toString()));
            return this;
        }

        public BlastBuilder gapopen(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(GAPOPEN, v.toString()));
            return this;
        }
    }

    public static class BlastEvent<E> {
        private final E cause;

        public BlastEvent(E event) {
            this.cause = event;
        }

        public Optional<E> getEvent() {
            return Optional.ofNullable(cause);
        }
    }

    public static interface BlastEventListner<E> {
        public int listen(BlastEvent<E> event);
    }

    public abstract int addListener(BlastEventListner<E> listner);

    public abstract int removeListener(BlastEventListner<E> listner);

    public abstract int notifyListeners(BlastEvent<E> event);
}
