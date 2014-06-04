package blast.blast.protein;

import blast.blast.AbstractBlast;
import blast.output.Iteration;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Created by alext on 6/4/14.
 * TODO document class
 */
public abstract class BlastP extends AbstractBlast<Iteration>{

    protected BlastP() {
    }
}
