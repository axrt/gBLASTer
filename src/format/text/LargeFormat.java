package format.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public interface LargeFormat extends Format {

    public boolean checkFormatting(InputStream toCheck) throws IOException;

    public String getAc(InputStream record) throws IOException;

    public void passSequence(InputStream record, OutputStream destination) throws IOException;

    public Stream<InputStream> iterateRecords(InputStream multiRecord, Path toTmpFile);
}
