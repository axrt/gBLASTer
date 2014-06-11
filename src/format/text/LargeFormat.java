package format.text;

import sequence.Sequence;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Created by alext on 6/10/14.
 * TODO document class
 */
public interface LargeFormat extends Format{

    public boolean checkFormatting(InputStream toCheck) throws Exception;
    public String getAc(InputStream record)throws Exception;
    public void passSequence(InputStream record, OutputStream destination)throws Exception;
    public Stream<InputStream> iterateRecords(InputStream multiRecord,Path toTmpFile);
}
