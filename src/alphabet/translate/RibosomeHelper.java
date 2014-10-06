package alphabet.translate;

import format.text.CommonFormats;
import sequence.protein.ORF;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by alext on 10/6/14.
 * TODO document class
 */
public class RibosomeHelper {

    private RibosomeHelper() {
        throw new AssertionError("Non-instantiable!");
    }


    public static Stream<ORF> readORFsFromFile(Path toReadFrom) throws IOException {

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(

                new Iterator<ORF>() {
                    final BufferedReader bufferedReader = new BufferedReader(new FileReader(toReadFrom.toFile()));
                    String line;
                    int i;
                    {
                        line = bufferedReader.readLine();
                        i=0;
                    }
                    @Override
                    public boolean hasNext() {

                        if (line == null||!line.startsWith(CommonFormats.Fasta.FASTA_START)) {
                            return false;
                        } else {
                            return true;
                        }

                    }

                    @Override
                    public ORF next() {

                        final String ac=new String(line.substring(1));
                        final StringBuilder stringBuilder = new StringBuilder();

                        try {
                            line=bufferedReader.readLine();
                            while (line!= null && !line.startsWith(CommonFormats.Fasta.FASTA_START)) {
                                stringBuilder.append(line);
                                line=bufferedReader.readLine();
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }

                        final String sequence=stringBuilder.toString();
                        final ORF orf=ORF.get(sequence,String.valueOf(i),0,sequence.length(),0);
                        i++;
                        return orf;
                    }

                }, Spliterator.NONNULL
        ), false);


    }

}
