package psimscan;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by alext on 4/6/15.
 */
public class QSimScanHelperTest {

    final Path toTestFile = Paths.get("db/orf/wolbachia_cu.fasta");
    final int numberOfFastasInBatch = 1000;

    @Test
    public void testSplitFasta() throws Exception {
        final List<Path> parts = QSimScanHelper.splitFasta(toTestFile, numberOfFastasInBatch);
        parts.stream().limit(parts.size() - 2).forEach(part -> {
                    try {
                        Assert.assertEquals(QSimScanHelper.numberOfFastas(part), numberOfFastasInBatch);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        parts.stream().forEach(part -> {
            Assert.assertTrue(part.toFile().delete());
        });
    }
}
