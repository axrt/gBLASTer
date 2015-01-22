package blast.blast;

import blast.ncbi.output.BlastOutput;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static blast.blast.BlastHelper.*;
import static blast.blast.BlastHelper.OUTFMT_VALS.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Created by alext on 5/27/14.
 * TODO document class
 */
public abstract class AbstractBlast<E> implements Callable<Optional<BlastOutput>> {
    protected List<BlastEventListner<E>> listeners;

    protected AbstractBlast() {
        this.listeners = new ArrayList<>();
    }

    public abstract static class BlastBuilder<E,T extends AbstractBlast<E>> {
        protected final Path pathToBlast;
        protected final Path queryFile;
        protected final String database;
        protected final Map<String, String> optionalParams;

        protected BlastBuilder(Path pathToBlast, Path queryFile, String database) {
            this.pathToBlast = pathToBlast;
            this.database = database;
            this.queryFile = queryFile;
            this.optionalParams = new LinkedHashMap<>();
        }
        protected abstract void die(Optional<Stream<String>> params, String addition);

        public BlastBuilder<E,T> query_loc(Optional<String> value) {
            value.ifPresent(v -> this.optionalParams.put(QUERY_LOC, v));
            return this;
        }

        public BlastBuilder<E,T> gilist(Optional<Path> value) {
            value.ifPresent(v -> this.optionalParams.put(GILIST, v.toFile().getPath()));
            return this;
        }

        public BlastBuilder<E,T> negative_gilist(Optional<Path> value) {
            value.ifPresent(v -> this.optionalParams.put(NEGATIVE_GILIST, v.toFile().getPath()));
            return this;
        }

        public BlastBuilder<E,T> remote (Optional<Boolean> value) {
            value.ifPresent(v -> {
                if(v) {
                    this.optionalParams.put(REMOTE, "");
                }
            });
            return this;
        }

        public BlastBuilder<E,T> strand(Optional<BlastHelper.STRAND_VALS> value) {
            value.ifPresent(v -> this.optionalParams.put(STRAND, v.toString()));
            return this;
        }
        public BlastBuilder<E,T> out(Optional<Path>value){
            value.ifPresent(v -> this.optionalParams.put(OUT, v.toFile().getPath()));
            return this;
        }

        public BlastBuilder<E,T> evalue(Optional<Double> value) {
            value.ifPresent(v -> this.optionalParams.put(EVALUE, v.toString()));
            return this;
        }
        public BlastBuilder<E,T> word_size(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(WORD_SIZE, v.toString()));
            return this;
        }

        public BlastBuilder<E,T> gapopen(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(GAPOPEN, v.toString()));
            return this;
        }
        public BlastBuilder<E,T> maxTargetSeqs(Optional<Integer> value){
            value.ifPresent(v->this.optionalParams.put(MAX_TARGET_SEQS,v.toString()));
            return this;
        }
        public BlastBuilder<E,T> subject(Optional<Path> value) {
            final List<String> has = Stream.of(DB, GILIST, SEQIDLIST, NEGATIVE_GILIST, DB_SOFT_MASK, DB_HARD_MASK).filter(s -> this.optionalParams.containsKey(s)).collect(toList());
            if (!has.isEmpty()) {
                value.ifPresent(v -> this.optionalParams.put(SUBJECT, v.toFile().getPath()));
                return this;
            }
            this.die(Optional.of(has.stream()), SUBJECT);
            return this;
        }
        public List<String> getCommand() {
            this.optionalParams.put(OUTFMT, BlastHelper.OUTFMT_VALS.XML.toString());
            final List<String> command = new ArrayList<>();
            command.add(this.pathToBlast.toFile().getPath());
            command.add(QUERY);
            command.add(this.queryFile.toFile().getPath());
            command.add(DB);
            command.add(this.database);
            this.optionalParams.entrySet().stream().forEach(e -> {
                command.add(e.getKey());
                command.add(e.getValue());
            });

            return command;
        }
        public BlastBuilder<E,T> num_threads(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(NUM_THREADS, String.valueOf(v.intValue())));
            return this;
        }
        public abstract T build();
    }

    public abstract static class BlastNBuilder<E,T extends AbstractBlast<E>> extends BlastBuilder<E,T>{

        public BlastNBuilder(Path pathToBlast, Path queryFile, String database) {
            super(pathToBlast, queryFile, database);
        }

        @Override
        protected void die(Optional<Stream<String>> params, String addition) {
            params.orElseThrow(() -> new IllegalArgumentException("Inconsistent parameter ".concat(addition).concat("!")));
            params.ifPresent(s -> new IllegalArgumentException("The BLASTN command already contains ".concat(s.collect(joining(", "))).concat(" commands, which are incompatible with ").concat(addition)));
        }

        public <BLASTN_TASK_VALS>BlastNBuilder<E,T> task(Optional<BLASTN_TASK_VALS> value) {
            value.ifPresent(v -> this.optionalParams.put(TASK, v.toString()));
            return this;
        }

        public BlastNBuilder<E,T> gapextend(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(GAPEXTEND, v.toString()));
            return this;
        }

        public BlastNBuilder<E,T> penalty(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(PENALTY, v.toString()));
            return this;
        }

        public BlastNBuilder<E,T> reward(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(REWARD, v.toString()));
            return this;
        }

        public BlastNBuilder<E,T> use_index(Optional<Boolean> value) {
            value.ifPresent(v -> this.optionalParams.put(USE_INDEX, v.toString()));
            return this;
        }

        public BlastNBuilder<E,T> index_name(Optional<String> value) {
            value.ifPresent(v -> this.optionalParams.put(INDEX_NAME, v));
            return this;
        }

        public BlastNBuilder<E,T> subject_loc(Optional<String> value) {
            final List<String> has = Stream.of(DB, GILIST, SEQIDLIST, NEGATIVE_GILIST, DB_SOFT_MASK, DB_HARD_MASK, REMOTE).filter(s -> this.optionalParams.containsKey(s)).collect(toList());
            if (!has.isEmpty()) {
                value.ifPresent(v -> this.optionalParams.put(SUBJECT, v));
                return this;
            }
            this.die(Optional.of(has.stream()), SUBJECT);
            return this;//will never be reached
        }

        public BlastNBuilder<E,T> outfmt(Optional<OUTFMT_VALS> value, Optional<OUTFMT_VALS.CUSTOM_FMT_VALS>... custom_fmt_vals) {
            value.ifPresent(v -> {
                final Set<OUTFMT_VALS> allowedVals;
                if (!(allowedVals = Stream.of(TABULAR, TABULAR_WITH_COMMENT_LINES, COMMA_SEP_VALS).collect(Collectors.toSet())).contains(v)) {
                    if (custom_fmt_vals != null) {
                        throw new IllegalArgumentException("Custom parameters allowed only with ".concat(allowedVals.stream().map(alv -> toString()).collect(joining(", "))).concat("!"));
                    }
                    this.optionalParams.put(OUTFMT, v.toString());
                } else {
                    if (custom_fmt_vals == null) {
                        this.optionalParams.put(OUTFMT, v.toString().concat(" ".concat(OUTFMT_VALS.CUSTOM_FMT_VALS.std.toString())));
                    } else {
                        this.optionalParams.put(OUTFMT, v.toString().concat(" ").concat(Arrays.asList(custom_fmt_vals).stream().filter(alv -> alv.isPresent()).map(alv -> alv.toString()).collect(joining(" "))));
                    }
                }
            });
            return this;
        }

        public BlastNBuilder<E,T> show_gis() {
            this.optionalParams.put(SHOW_GIS, "");
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

    public abstract static class BlastPBuilder<E,T extends AbstractBlast<E>> extends BlastBuilder<E,T>{
        public BlastPBuilder(Path pathToBlast, Path queryFile, String database) {
            super(pathToBlast, queryFile, database);
        }
        @Override
        protected void die(Optional<Stream<String>> params, String addition) {
            params.orElseThrow(() -> new IllegalArgumentException("Inconsistent parameter ".concat(addition).concat("!")));
            params.ifPresent(s -> new IllegalArgumentException("The BLASTN command already contains ".concat(s.collect(joining(", "))).concat(" commands, which are incompatible with ").concat(addition)));
        }
     //Specific methods

    }

    public static interface BlastEventListner<E>{
        public int listen(BlastEvent<E> event) throws Exception;
    }

    public abstract int addListener(BlastEventListner<E> listner);

    public abstract int removeListener(BlastEventListner<E> listner);

    public abstract int notifyListeners(BlastEvent<E> event) throws Exception;
}
