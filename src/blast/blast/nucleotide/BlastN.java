package blast.blast.nucleotide;

import blast.blast.AbstractBlast;
import blast.blast.BlastHelper;

import java.nio.file.Path;
import java.util.*;
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
public abstract class BlastN<E> extends AbstractBlast<E> {


    public static abstract class BlastNBuilder extends AbstractBlast.BlastBuilder {

        protected BlastNBuilder(Path pathToBlast, Path queryFile, String database) {
            super(pathToBlast, queryFile, database);
        }
        @Override
        protected void die(Optional<Stream<String>> params, String addition) {
            params.orElseThrow(() -> new IllegalArgumentException("Inconsistent parameter ".concat(addition).concat("!")));
            params.ifPresent(s -> new IllegalArgumentException("The BLASTN command already contains ".concat(s.collect(joining(", "))).concat(" commands, which are incompatible with ").concat(addition)));
        }

        public BlastNBuilder strand(Optional<BlastHelper.STRAND_VALS> value) {
            value.ifPresent(v -> this.optionalParams.put(STRAND, v.toString()));
            return this;
        }
        @Override
        public <BLASTN_TASK_VALS>BlastNBuilder task(Optional<BLASTN_TASK_VALS> value) {
            value.ifPresent(v -> this.optionalParams.put(TASK, v.toString()));
            return this;
        }

        public BlastNBuilder gapextend(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(GAPEXTEND, v.toString()));
            return this;
        }

        public BlastNBuilder penalty(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(PENALTY, v.toString()));
            return this;
        }

        public BlastNBuilder reward(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(REWARD, v.toString()));
            return this;
        }

        public BlastNBuilder use_index(Optional<Boolean> value) {
            value.ifPresent(v -> this.optionalParams.put(USE_INDEX, v.toString()));
            return this;
        }

        public BlastNBuilder index_name(Optional<String> value) {
            value.ifPresent(v -> this.optionalParams.put(INDEX_NAME, v));
            return this;
        }

        public BlastNBuilder subject(Optional<Path> value) {
            final List<String> has = Stream.of(DB, GILIST, SEQIDLIST, NEGATIVE_GILIST, DB_SOFT_MASK, DB_HARD_MASK).filter(s -> this.optionalParams.containsKey(s)).collect(toList());
            if (!has.isEmpty()) {
                value.ifPresent(v -> this.optionalParams.put(SUBJECT, v.toFile().getPath()));
                return this;
            }
            this.die(Optional.of(has.stream()), SUBJECT);
            return this;
        }

        public BlastNBuilder subject_loc(Optional<String> value) {
            final List<String> has = Stream.of(DB, GILIST, SEQIDLIST, NEGATIVE_GILIST, DB_SOFT_MASK, DB_HARD_MASK, REMOTE).filter(s -> this.optionalParams.containsKey(s)).collect(toList());
            if (!has.isEmpty()) {
                value.ifPresent(v -> this.optionalParams.put(SUBJECT, v));
                return this;
            }
            this.die(Optional.of(has.stream()), SUBJECT);
            return this;//will never be reached
        }

        public BlastNBuilder outfmt(Optional<OUTFMT_VALS> value, Optional<OUTFMT_VALS.CUSTOM_FMT_VALS>... custom_fmt_vals) {
            value.ifPresent(v -> {
                final Set<OUTFMT_VALS> allowedVals;
                if (!(allowedVals = Stream.of(TABULAR, TABULAR_WITH_COMMENT_LINES, COMMA_SEP_VALS).collect(Collectors.toSet())).contains(v)) {
                    if (custom_fmt_vals != null) {
                        throw new IllegalArgumentException("Custom parameters allowed only with ".concat(allowedVals.stream().map(alv -> toString()).collect(joining(", "))).concat("!"));
                    }
                    this.optionalParams.put(OUTFMT, v.toString());
                } else {
                    if (custom_fmt_vals == null) {
                        this.optionalParams.put(OUTFMT, v.toString().concat(" ".concat(CUSTOM_FMT_VALS.std.toString())));
                    } else {
                        this.optionalParams.put(OUTFMT, v.toString().concat(" ").concat(Arrays.asList(custom_fmt_vals).stream().filter(alv -> alv.isPresent()).map(alv -> alv.toString()).collect(joining(" "))));
                    }
                }
            });
            return this;
        }

        public BlastNBuilder show_gis() {
            this.optionalParams.put(SHOW_GIS, "");
            return this;
        }

        public BlastNBuilder num_threads(Optional<Integer> value) {
            value.ifPresent(v -> this.optionalParams.put(NUM_THREADS, v.toString()));
            return this;
        }
        public abstract BlastN build();

    }
}
